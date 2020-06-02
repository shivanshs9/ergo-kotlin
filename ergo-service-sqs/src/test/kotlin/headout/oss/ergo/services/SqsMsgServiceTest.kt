package headout.oss.ergo.services

import com.google.common.truth.Truth.assertThat
import headout.oss.ergo.BaseTest
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.future.await
import org.junit.Assert
import org.junit.Test
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse
import java.util.concurrent.CompletableFuture

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
    }

    @Test
    fun sufficientWorkersLaunchedOnStart() {
        msgService.start().apply {
            val workersCount = BaseMsgService.DEFAULT_NUMBER_WORKERS
            assertThat(children.count()).isEqualTo(workersCount)
        }
    }

    @Test
    fun invalidMessageWithoutGroupId_HandlesMissingKeyException() {
        mockReceiveMessageResponse()
        msgService.start()
        verify {
            BaseMsgService.handleException(match {
                it is IllegalStateException && it.message == "Message doesn't have 'MessageGroupId' key!"
            })
        }
    }

    @Test
    fun invalidMessageWithoutBody_WorkerProcessRequestAndHandlesException() {
        mockReceiveMessageResponse(taskId = "invalidTask")
        msgService.start()
        coVerify {
            msgService.processRequest(any())
        }
        verify {
            BaseMsgService.handleException(match {
                it is IllegalStateException && it.message == "request.message.body() must not be null"
            })
        }
    }

    @Test
    fun validMessageButInvalidTaskId_HandlesTaskNotFoundException() {
        mockReceiveMessageResponse(taskId = "taskNotFound", body = "false")
        msgService.start()
        coVerify {
            msgService.processRequest(any())
        }
    }

    private fun mockReceiveMessageResponse(jobId: String = "jobId", taskId: String? = null, body: String? = null) {
        val response = ReceiveMessageResponse.builder()
            .messages(
                Message.builder()
                    .messageId(jobId)
                    .apply {
                        taskId?.let {
                            attributes(mapOf(MessageSystemAttributeName.MESSAGE_GROUP_ID to it))
                        }
                    }
                    .apply {
                        body?.let { body(body) }
                    }
                    .build()
            )
            .build()
        every { sqsClient.receiveMessage(any<ReceiveMessageRequest>()) } returns CompletableFuture.completedFuture(
            response
        )
    }

    companion object {
        const val QUEUE_URL = "sqs://queue"
    }
}