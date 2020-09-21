package headout.oss.ergo

import headout.oss.ergo.helpers.ImmediateRespondJobResultHandler
import headout.oss.ergo.services.SqsMsgService
import kotlinx.coroutines.*
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import java.net.URI

/**
 * Created by shivanshs9 on 05/06/20.
 */
val TASKS = listOf("spring_optionalArg", "suspend_oneArg")
val TASK_BODIES = listOf(
    "{}", "{\"num\": 3}"
)
val LOCAL_REGION: Region = Region.of("default")
val LOCAL_ENDPOINT: URI = URI.create("http://localhost:9324")
const val QUEUE_URL = "http://localhost:9324/queue/fifo_req"
const val RESULT_QUEUE_URL = "http://localhost:9324/queue/fifo_res"

fun CoroutineScope.produceTasks() = launch {
    println("${Thread.currentThread().name} Producing tasks")
    val sqsClient = SqsClient.builder()
        .endpointOverride(LOCAL_ENDPOINT)
        .region(LOCAL_REGION)
        .build()

    var i = 0
    while (isActive) {
        val taskIndex = TASKS.indices.random()
        val sendMsg = SendMessageRequest.builder()
            .messageGroupId(TASKS[taskIndex])
            .messageBody(TASK_BODIES.random())
            .queueUrl(QUEUE_URL)
            .messageDeduplicationId(i.toString())
            .build()
        val id = sqsClient.sendMessage(sendMsg).messageId()
        println("Message sent with id: $id")
        delay((1000L..5000L).random())
        i++
    }
}

fun main() = runBlocking {
    val springApp = MySpringApplication()
    springApp.main()

    println("${Thread.currentThread().name} Starting program")
    val job = produceTasks()
    val sqsClient = SqsAsyncClient.builder()
        .endpointOverride(LOCAL_ENDPOINT)
        .region(LOCAL_REGION)
        .build()
    val service = SqsMsgService(
        sqsClient,
        QUEUE_URL,
        RESULT_QUEUE_URL,
        numWorkers = 20,
        resultHandler = ImmediateRespondJobResultHandler()
    )
    val cronJob = service.start()
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            super.run()
            service.stop()
            springApp.stop()
            job.cancel()
        }
    })
    cronJob.join()
    service.stop()
}