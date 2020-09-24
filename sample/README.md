## Ergo client Samples

You can find some sample tasks to help guide you in integrating ergo-kotlin in your project.

### Providing Task Metadata in codebase

You can find some [sample task definitions](//github.com/headout/ergo-kotlin/src/main/kotlin/headout/oss/ergo/ExampleTasks.kt) for static functions and suspending functions.

- Annotate the relevant functions with `Task` and provide a suitable **taskId** as argument (will be later used in code generation to map the taskId to this function call)

```kotlin
import headout.oss.ergo.annotations.Task

class ExampleTasks {
    @Task("noArg")
    fun noArg(): Boolean {
      // some long running execution
      return true
    }
}
```

- If using custom data class as task parameter, make sure it's serializable. The return type must also be serializable:

```kotlin
import headout.oss.ergo.annotations.Task
import kotlinx.serialization.Serializable

object ExampleTasks {
    @Task("serializableArg")
    @JvmStatic
    fun serializableArg(request: Request): Result {
      return Result(request.somethingImportant)
    }
}
@Serializable
data class Request(val somethingImportant: Int)

@Serializable
data class Result(val number: Int)
```

Here's an example for a suspending function task:

```kotlin
import headout.oss.ergo.annotations.Task
import kotlinx.coroutines.delay

class ExampleTasks {
    @Task("suspendingTask")
    suspend fun longRunningTask(input: Int): Int {
      delay(20000L)
      return input * input
    }
}
```

### Spring Tasks
You can find some [sample task defintions](//github.com/headout/ergo-kotlin/src/main/kotlin/headout/oss/ergo/SpringTasks.kt) for a task in spring service.
The constructor also has an `@Autowired` property for a sample spring bean.

### Using Ergo's Message Service

#### 1. SQS service
You can find the sample code for service [here](//github.com/headout/ergo-kotlin/src/main/kotlin/headout/oss/ergo/Runtime.kt).

- Create SQS client and SQS Message Service (with default number of workers, 8, and result handler, In-Memory Buffer Results):

```kotlin
const val AWS_REGION: Region = Region.US_EAST_1
// The following Queues must be of FIFO queue type since only FIFO queue supports MessageGroupId
const val REQUEST_QUEUE_URL = "..."
const val RESULT_QUEUE_URL = "..."

val sqsClient = SqsAsyncClient.builder()
    .region(AWS_REGION)
    .build()
val service = SqsMsgService(sqsClient, REQUEST_QUEUE_URL, RESULT_QUEUE_URL)
```

- Start the message service on application start:

```kotlin
service.start() // Launches bunch of coroutines
```

- When needed, stop the message service:

```kotlin
service.stop()
```

- To add a graceful shutdown of all worker coroutines, use `Runtime.addShutdownHook` function:

```kotlin
Runtime.getRuntime().addShutdownHook(object : Thread() {
    override fun run() {
        super.run()
        service.stop()
    }
})
```