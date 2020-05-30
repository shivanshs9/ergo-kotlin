package headout.oss.ergo.services

import headout.oss.ergo.annotations.TaskId
import headout.oss.ergo.factory.JobParser
import headout.oss.ergo.models.JobId
import headout.oss.ergo.models.JobResult
import headout.oss.ergo.models.RequestMsg
import headout.oss.ergo.utils.repeatUntilCancelled
import headout.oss.ergo.utils.sendDelayed
import headout.oss.ergo.utils.ticker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.*
import java.util.concurrent.TimeUnit
import kotlin.math.round

/**
 * Created by shivanshs9 on 28/05/20.
 */
@ExperimentalCoroutinesApi
class SqsMsgService(
    private val sqs: SqsAsyncClient,
    private val requestQueueUrl: String,
    private val resultQueueUrl: String = requestQueueUrl,
    private val defaultVisibilityTimeout: Long = DEFAULT_VISIBILITY_TIMEOUT
) : BaseMsgService<Message>() {
    private val defaultPingMessageDelay: Long by lazy { getPingDelay(defaultVisibilityTimeout) }
    private val pendingJobs = mutableSetOf<JobId>()

    private val timeoutResultCollect = ticker(TIMEOUT_RESULT_COLLECTION)

    private val receiveRequest by lazy {
        ReceiveMessageRequest.builder()
            .messageAttributeNames(MessageSystemAttributeName.MESSAGE_GROUP_ID.toString())
            .queueUrl(requestQueueUrl)
            .maxNumberOfMessages(MAX_BUFFERED_MESSAGES)
            .waitTimeSeconds(20)
            .build()
    }

    override suspend fun processRequest(request: RequestMsg<Message>): JobResult<*> {
        val result = jobController.runJob(request.taskId, request.jobId, request.message.body())
        pendingJobs.remove(result.jobId)
        return result
    }

    override suspend fun collectRequests(): ReceiveChannel<RequestMsg<Message>> =
        produce(Dispatchers.IO, CAPACITY_REQUEST_BUFFER) {
            repeatUntilCancelled {
                val messages = sqs.receiveMessage(receiveRequest).await().messages()
                for (msg in messages) {
                    val groupId = msg.attributes()[MessageSystemAttributeName.MESSAGE_GROUP_ID]
                        ?: error("Message doesn't have 'MessageGroupId' key!")
                    val requestMsg = RequestMsg(toTaskId(groupId), msg.messageId(), msg)
                    pendingJobs.add(requestMsg.jobId)
                    send(requestMsg)
                    captures.sendDelayed(PingMessageCapture(requestMsg), defaultPingMessageDelay)
                }
            }
        }

    override suspend fun handleCaptures(): Job = launch(Dispatchers.IO) {
        val bufferedResults = mutableListOf<JobResult<*>>()
        repeatUntilCancelled {
            select {
                captures.onReceive {
                    when (it) {
                        is ErrorResultCapture -> handleError(it)
                        is SuccessResultCapture -> handleSuccess(it)
                        is RespondResultCapture -> {
                            bufferedResults.add(it.result)
                            if (bufferedResults.size == MAX_BUFFERED_MESSAGES) {
                                pushResults(bufferedResults)
                                bufferedResults.clear()
                            }
                        }
                        is PingMessageCapture -> if (pendingJobs.contains(it.request.jobId)) {
                            val newTimeout = defaultVisibilityTimeout * (it.attempt + 1)
                            changeVisibilityTimeout(it.request, newTimeout.toInt())
                            captures.sendDelayed(
                                PingMessageCapture(it.request, it.attempt + 1),
                                defaultPingMessageDelay
                            )
                        }
                    }
                }
                timeoutResultCollect.onReceive {
                    pushResults(bufferedResults)
                    bufferedResults.clear()
                }
            }
        }
    }

    private suspend fun handleError(resultCapture: ErrorResultCapture<Message>) = deleteMessage(resultCapture.request)

    private suspend fun handleSuccess(resultCapture: SuccessResultCapture<Message>) =
        deleteMessage(resultCapture.request)

    private suspend fun pushResults(jobResults: List<JobResult<*>>) {
        val msgEntries = jobResults.map {
            val msgBody = parseResult(it)
            SendMessageBatchRequestEntry.builder()
                .messageBody(msgBody)
                .messageGroupId(it.taskId)
                .build()
        }
        val sendRequest = SendMessageBatchRequest.builder()
            .queueUrl(resultQueueUrl)
            .entries(msgEntries)
            .build()

        val response = sqs.sendMessageBatch(sendRequest).await()
        response.successful().forEach {
            println("Response message '${it.messageId()}' successfully sent!")
        }
        response.failed().forEach {
            println("Response message, indexed '${it.id()}', failed to send with code '${it.code()}'!")
        }
    }

    private suspend fun changeVisibilityTimeout(request: RequestMsg<Message>, visibilityTimeout: Int) {
        sqs.changeMessageVisibility {
            it.queueUrl(requestQueueUrl)
            it.receiptHandle(request.message.receiptHandle())
            it.visibilityTimeout(visibilityTimeout)
        }.await()
    }

    private suspend fun deleteMessage(request: RequestMsg<Message>) {
        sqs.deleteMessage {
            it.queueUrl(requestQueueUrl)
            it.receiptHandle(request.message.receiptHandle())
        }.await()
    }

    companion object : TaskServiceConversion {
        init {
            jobController.parser = JobParser
        }

        const val MAX_BUFFERED_MESSAGES = 10
        val DEFAULT_VISIBILITY_TIMEOUT = TimeUnit.MINUTES.toSeconds(20)
        val TIMEOUT_RESULT_COLLECTION: Long = TimeUnit.MINUTES.toMillis(10)

        override fun toTaskId(value: String): TaskId = value

        override fun fromTaskId(taskId: TaskId): String = taskId

        fun getPingDelay(visibilityTimeout: Long) = round(visibilityTimeout * 0.75).toLong()
    }
}