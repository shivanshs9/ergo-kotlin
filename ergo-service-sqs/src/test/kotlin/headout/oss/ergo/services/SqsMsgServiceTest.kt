package headout.oss.ergo.services

import com.google.common.truth.Truth.assertThat
import headout.oss.ergo.BaseTest
import headout.oss.ergo.models.JobResult
import headout.oss.ergo.models.JobResultMetadata
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.junit.Test
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

/**
 * Created by shivanshs9 on 02/06/20.
 */
@ExperimentalCoroutinesApi
class SqsMsgServiceTest : BaseTest() {
    private val sqsClient: SqsAsyncClient = mockk(relaxed = true)
    private lateinit var msgService: SqsMsgService

    override fun beforeTest() {
        super.beforeTest()
        msgService = spyk(SqsMsgService(sqsClient, QUEUE_URL, scope = testScope))
        mockkObject(BaseMsgService.Companion)
        mockCommonSqsClient()
    }

    override fun afterTest() {
        super.afterTest()
        msgService.stop()
    }

    @Test
    fun sufficientWorkersLaunchedOnStart() {
        msgService.start().apply {
            val workersCount = BaseMsgService.DEFAULT_NUMBER_WORKERS
            assertThat(children.count()).isEqualTo(workersCount)
        }
        val countLauncher = 1
        val countChannels = 2
        val countWorkerSupervisor = 1
        assertThat(
            msgService.coroutineContext[Job]?.children?.count() ?: 0
        ).isEqualTo(countChannels + countLauncher + countWorkerSupervisor)
    }

    @Test
    fun invalidMessageWithoutGroupId_HandlesMissingKeyException() {
        mockReceiveMessageResponse()
        msgService.start()
        verify {
            BaseMsgService.collectCaughtExceptions(match {
                it is IllegalStateException && it.message == "Message doesn't have 'MessageGroupId' key!"
            })
        }
    }

    @Test
    fun invalidMessageWithoutBody_VerifyInvalidRequestResult() {
        mockReceiveMessageResponse(taskId = "invalidTask")
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(DELAY_WAIT)
        }
        verify {
            msgService["handleError"](match<ErrorResultCapture<Message>> {
                val metadata = it.result.metadata
                metadata.status == JobResultMetadata.STATUS.ERROR_INVALID_REQUEST.code && metadata.error?.message == "message.body() must not be null"
            })
        }
    }

    @Test
    fun validMessageButInvalidTaskId_VerifyTaskNotFoundResult() {
        val taskId = "taskNotFound"
        mockReceiveMessageResponse(taskId = taskId, body = "false")
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(DELAY_WAIT)
        }
        verify {
            msgService["handleError"](match<ErrorResultCapture<Message>> {
                val metadata = it.result.metadata
                metadata.status == JobResultMetadata.STATUS.ERROR_NOT_FOUND.code && metadata.error?.message == "Could not find relevant function for the taskId - '$taskId'"
            })
        }
    }

    @Test
    fun whenTaskValidButRequestDataInvalid_VerifyParseErrorResult() {
        val taskId = "xyz.1"
        mockReceiveMessageResponse(taskId = taskId, body = "false")
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(DELAY_WAIT)
        }
        verify {
            msgService["handleError"](match<ErrorResultCapture<Message>> {
                val metadata = it.result.metadata
                metadata.status == JobResultMetadata.STATUS.ERROR_PARSE.code
            })
        }
    }

    @Test
    fun whenWorkerEncounteredError_VerifyDeleteMessageRequest() {
        val taskId = "taskNotFound"
        val receiptHandle = "receipt"
        mockReceiveMessageResponse(taskId = taskId, body = "false", receiptHandle = receiptHandle)
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(DELAY_WAIT)
        }
        verify {
            sqsClient.deleteMessage(match<DeleteMessageRequest> {
                it.receiptHandle() == receiptHandle
            })
        }
    }

    @Test
    fun whenTaskRequestValidAndRanSuccessfully_VerifySuccessResult() {
        val taskId = "xyz.1"
        val body = "{\"i\": 1, \"hi\": \"whatever\"}"
        mockReceiveMessageResponse(taskId = taskId, body = body)
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(DELAY_WAIT)
        }
        verify {
            msgService["handleSuccess"](match<SuccessResultCapture<Message>> {
                val metadata = it.result.metadata
                metadata.status == JobResultMetadata.STATUS.SUCCESS.code
            })
        }
    }

    @Test
    fun whenWorkerEncounteredErrorAndBufferResultsToMax_PushToResultQueue() {
        val msgCount = SqsMsgService.MAX_BUFFERED_MESSAGES
        val taskId = "taskNotFound"
        val receiptHandle = "receipt"
        mockReceiveMessageResponse(taskId = taskId, body = "false", receiptHandle = receiptHandle, msgCount = msgCount)
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(DELAY_WAIT * msgCount)
            msgService["pushResults"](match<List<JobResult<*>>> {
                it.size == msgCount
            })
        }
    }

    @Test
    fun whenWorkerEncounteredErrorAndBufferResultsInsufficientAndTimeoutHappened_PushToResultQueue() {
        val msgCount = SqsMsgService.MAX_BUFFERED_MESSAGES - 1
        val taskId = "taskNotFound"
        val receiptHandle = "receipt"
        mockReceiveMessageResponse(taskId = taskId, body = "false", receiptHandle = receiptHandle, msgCount = msgCount)
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(SqsMsgService.TIMEOUT_RESULT_COLLECTION) // time consuming since timeout value is 2 minutes
            msgService["pushResults"](match<List<JobResult<*>>> {
                it.size == msgCount
            })
        }
    }

    @Test
    fun whenTaskRequestValidAndRanSuccessfullyAndBufferResultsToMax_PushToResultQueue() {
        val msgCount = SqsMsgService.MAX_BUFFERED_MESSAGES
        val taskId = "xyz.1"
        val body = "{\"i\": 1, \"hi\": \"whatever\"}"
        mockReceiveMessageResponse(taskId = taskId, body = body, msgCount = msgCount)
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(DELAY_WAIT * msgCount)
            msgService["pushResults"](match<List<JobResult<*>>> {
                it.size == msgCount
            })
        }
    }

    @Test
    fun whenTaskRequestValidAndRanSuccessfullyAndBufferResultsInsufficientAndTimeoutHappened_PushToResultQueue() {
        val msgCount = SqsMsgService.MAX_BUFFERED_MESSAGES - 1
        val taskId = "xyz.1"
        val body = "{\"i\": 1, \"hi\": \"whatever\"}"
        mockReceiveMessageResponse(taskId = taskId, body = body, msgCount = msgCount)
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(SqsMsgService.TIMEOUT_RESULT_COLLECTION) // time consuming since timeout value is 2 minutes
            msgService["pushResults"](match<List<JobResult<*>>> {
                it.size == msgCount
            })
        }
    }

    @Test
    fun whenPushResultsCalled_BatchSendMessageToSqsQueue() {
        val msgCount = SqsMsgService.MAX_BUFFERED_MESSAGES
        val taskId = "noArgWithSerializableResult"
        val body = ""
        mockReceiveMessageResponse(taskId = taskId, body = body, msgCount = msgCount)
        msgService.start()
        coVerify {
            msgService.processRequest(any())
            delay(DELAY_WAIT * msgCount)
            msgService["pushResults"](match<List<JobResult<*>>> {
                it.size == msgCount
            })
        }
        verify {
            sqsClient.sendMessageBatch(match<SendMessageBatchRequest> {
                val entries = it.entries()
                println(entries)
                it.queueUrl() == QUEUE_URL && entries.size == msgCount
            })
        }
    }

    private fun mockCommonSqsClient() {
        val deleteSlot = slot<Consumer<DeleteMessageRequest.Builder>>()
        every { sqsClient.deleteMessage(capture(deleteSlot)) } answers {
            callOriginal()
        }
    }

    private fun mockReceiveMessageResponse(
        jobId: String = "jobId",
        taskId: String? = null,
        body: String? = null,
        receiptHandle: String = "receipt",
        msgCount: Int = 1
    ) {
        val response = ReceiveMessageResponse.builder()
            .messages(
                List(msgCount) {
                    Message.builder()
                        .messageId("$jobId-$it")
                        .apply {
                            taskId?.let {
                                attributes(mapOf(MessageSystemAttributeName.MESSAGE_GROUP_ID to it))
                            }
                        }
                        .apply {
                            body?.let { body(body) }
                        }
                        .receiptHandle(receiptHandle)
                        .build()
                }
            )
            .build()
        every { sqsClient.receiveMessage(any<ReceiveMessageRequest>()) } returnsMany listOf<CompletableFuture<ReceiveMessageResponse>>(
            CompletableFuture.completedFuture(response),
            CompletableFuture.supplyAsync {
                Thread.sleep(SqsMsgService.TIMEOUT_RESULT_COLLECTION * 2) // to ensure the error happens only after results are pushed
                error("Dummy error to mark failure on receiving messages")
            }
        )
    }

    companion object {
        private const val QUEUE_URL = "sqs://queue"
        private const val DELAY_WAIT = 1000L
    }
}