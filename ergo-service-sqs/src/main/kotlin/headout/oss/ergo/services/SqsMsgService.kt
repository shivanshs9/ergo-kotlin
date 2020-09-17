package headout.oss.ergo.services

import headout.oss.ergo.annotations.TaskId
import headout.oss.ergo.models.JobId
import headout.oss.ergo.models.JobResult
import headout.oss.ergo.models.RequestMsg
import headout.oss.ergo.utils.asyncSendDelayed
import headout.oss.ergo.utils.repeatUntilCancelled
import headout.oss.ergo.utils.ticker
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.future.await
import kotlinx.coroutines.selects.select
import mu.KotlinLogging
import org.slf4j.MarkerFactory
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.*
import java.util.concurrent.TimeUnit
import kotlin.math.round
import kotlin.properties.Delegates

/**
 * Created by shivanshs9 on 28/05/20.
 */
private val logger = KotlinLogging.logger {}

@ExperimentalCoroutinesApi
class SqsMsgService(
    private val sqs: SqsAsyncClient,
    private val requestQueueUrl: String,
    private val resultQueueUrl: String = requestQueueUrl,
    private val defaultVisibilityTimeout: Long? = null,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : BaseMsgService<Message>(scope) {
    private var visibilityTimeout by Delegates.notNull<Long>()

    private val defaultPingMessageDelay: Long by lazy { getPingDelay(visibilityTimeout) }
    private val pendingJobs = mutableSetOf<JobId>()

    private val timeoutResultCollect = ticker(TIMEOUT_RESULT_COLLECTION)

    private val receiveRequest by lazy {
        ReceiveMessageRequest.builder()
            .attributeNamesWithStrings(MessageSystemAttributeName.MESSAGE_GROUP_ID.toString())
            .queueUrl(requestQueueUrl)
            .maxNumberOfMessages(MAX_BUFFERED_MESSAGES)
            .waitTimeSeconds(20)
            .build()
    }

    override suspend fun initService() {
        visibilityTimeout = defaultVisibilityTimeout?.also {
            logger.info { "Using the visibility timeout of $it seconds" }
        } ?: sqs.runCatching {
            logger.info { "Attempting to fetch visibility timeout..." }
            getVisibilityTimeout(requestQueueUrl).also {
                logger.info { "Fetched visibility timeout of $it seconds" }
            }
        }.getOrElse {
            logger.warn(it) { "Failed to fetch!!" }
            DEFAULT_VISIBILITY_TIMEOUT.also {
                logger.info { "Using default visibility timeout of $it seconds" }
            }
        }
    }

    override suspend fun processRequest(request: RequestMsg<Message>): JobResult<*> = try {
        jobController.runJob(request.taskId, request.jobId, request.message.body())
    } finally {
        pendingJobs.remove(request.jobId)
    }

    override suspend fun collectRequests(): ReceiveChannel<RequestMsg<Message>> =
        produce(Dispatchers.IO, CAPACITY_REQUEST_BUFFER) {
            repeatUntilCancelled(BaseMsgService.Companion::collectCaughtExceptions) {
                val messages = sqs.receiveMessage(receiveRequest).await().messages()
//                val cleanLog: (() -> String) -> Unit = if (messages.isNotEmpty()) logger::info else logger::debug
                logger.info { "Received ${messages.size} messages" }
                for (msg in messages) {
                    val groupId = msg.attributes()[MessageSystemAttributeName.MESSAGE_GROUP_ID]
                        ?: error("Message doesn't have 'MessageGroupId' key!")
                    val requestMsg = RequestMsg(toTaskId(groupId), msg.messageId(), msg).also {
                        logger.debug { "Received request - $it" }
                    }
                    pendingJobs.add(requestMsg.jobId)
                    send(requestMsg)
                    asyncSendDelayed(captures, PingMessageCapture(requestMsg), defaultPingMessageDelay)
                }
            }
        }

    override suspend fun handleCaptures(): Job = launch(Dispatchers.IO) {
        val bufferedResults = mutableListOf<JobResult<*>>()
        repeatUntilCancelled(BaseMsgService.Companion::collectCaughtExceptions) {
            select {
                captures.onReceive { capture ->
                    logger.info("event: '${capture::class.simpleName}', request: '${capture.request}'")
                    when (capture) {
                        is ErrorResultCapture -> handleError(capture)
                        is SuccessResultCapture -> handleSuccess(capture)
                        is RespondResultCapture -> {
                            bufferedResults.add(capture.result)
                            logger.debug(MARKER_RESULT_BUFFER) { "Added result of job '${capture.result.jobId}' to buffer (size = ${bufferedResults.size})" }
                            if (bufferedResults.size == MAX_BUFFERED_MESSAGES) {
                                pushResults(bufferedResults.toList())
                                bufferedResults.clear()
                            }
                        }
                        is PingMessageCapture -> if (pendingJobs.contains(capture.request.jobId)) {
                            logger.debug(MARKER_RESULT_BUFFER) { "PING: job '${capture.request.jobId}' still pending (attempt ${capture.attempt})" }
                            val newTimeout =
                                getVisibilityTimeoutForAttempt(visibilityTimeout, capture.attempt)
                            changeVisibilityTimeout(capture.request, newTimeout)
                            asyncSendDelayed(
                                captures,
                                PingMessageCapture(capture.request, capture.attempt + 1),
                                defaultPingMessageDelay
                            )
                        } else logger.debug(MARKER_RESULT_BUFFER) { "PING: jobs - $pendingJobs" }
                    }
                }
                timeoutResultCollect.onReceive {
                    if (bufferedResults.isNotEmpty()) {
                        logger.debug(MARKER_RESULT_BUFFER, "TIMEOUT: Result Collect!")
                        pushResults(bufferedResults.toList())
                        bufferedResults.clear()
                    }
                }
            }
        }
    }

    private suspend fun handleError(resultCapture: ErrorResultCapture<Message>) = deleteMessage(resultCapture.request)

    private suspend fun handleSuccess(resultCapture: SuccessResultCapture<Message>) =
        deleteMessage(resultCapture.request)

    private suspend fun pushResults(jobResults: List<JobResult<*>>) = launch(Dispatchers.IO) {
        logger.info { "Pushing ${jobResults.size} results!" }
        val msgEntries = jobResults.mapIndexed { index, jobResult ->
            val msgBody = parseResult(jobResult)
            SendMessageBatchRequestEntry.builder()
                .id(index.toString())
                .messageBody(msgBody)
                .messageGroupId(jobResult.taskId)
                .build()
        }
        val sendRequest = SendMessageBatchRequest.builder()
            .queueUrl(resultQueueUrl)
            .entries(msgEntries)
            .build()

        val response = sqs.sendMessageBatch(sendRequest).await()
        response.successful().forEach {
            logger.info {
                "Response message '${it.messageId()}' for '${jobResults[it.id().toInt()]}' successfully sent!"
            }
        }
        response.failed().forEach {
            logger.warn {
                "Response message, indexed '${it.id()}' for '${jobResults[it.id()
                    .toInt()]}', failed to send with code '${it.code()}'!"
            }
        }
    }

    private suspend fun changeVisibilityTimeout(request: RequestMsg<Message>, visibilityTimeout: Long) =
        launch(Dispatchers.IO) {
            logger.info { "Change visibility timeout of message with jobId '${request.jobId}' to $visibilityTimeout seconds" }
            sqs.changeMessageVisibility {
                it.queueUrl(requestQueueUrl)
                it.receiptHandle(request.message.receiptHandle())
                it.visibilityTimeout(visibilityTimeout.toInt())
            }.await()
        }

    private suspend fun deleteMessage(request: RequestMsg<Message>) = launch(Dispatchers.IO) {
        logger.info { "Deleting message with jobId - ${request.jobId}" }
        sqs.deleteMessage {
            it.queueUrl(requestQueueUrl)
            it.receiptHandle(request.message.receiptHandle())
        }.await()
    }

    companion object : TaskServiceConversion {
        const val MAX_BUFFERED_MESSAGES = 10
        private const val MAX_VISIBILITY_TIMEOUT = 43199.toLong()
        val DEFAULT_VISIBILITY_TIMEOUT = TimeUnit.SECONDS.toSeconds(30)
        val TIMEOUT_RESULT_COLLECTION: Long = TimeUnit.MINUTES.toMillis(2)

        private val MARKER_RESULT_BUFFER = MarkerFactory.getMarker("Buffer")

        override fun toTaskId(value: String): TaskId = value

        override fun fromTaskId(taskId: TaskId): String = taskId

        fun getPingDelay(visibilityTimeout: Long) = TimeUnit.SECONDS.toMillis(round(visibilityTimeout * 0.75).toLong())

        fun getVisibilityTimeoutForAttempt(visibilityTimeout: Long, attempt: Int) =
            (visibilityTimeout * (attempt + 1)).coerceAtMost(MAX_VISIBILITY_TIMEOUT)
    }
}