## ergo-service-sqs

Module defining SQS Message service to communicate with SQS FIFO queues, using AWS Java SDK v2, to pick up tasks
and push job results
Requires `"software.amazon.awssdk:sqs"` to be added as implementation dependency in your project

### Installation

Add following to Gradle:

<details open>
<summary>Kotlin DSL (build.gradle.kts)</summary>

```kotlin
dependencies {
  implementation(platform("software.amazon.awssdk:bom:2.13.26"))
  implementation("software.amazon.awssdk:sqs")
  implementation("com.github.headout.ergo-kotlin:ergo-service-sqs:1.2.0")
}
```

</details>

<details>
<summary>Groovy (build.gradle)</summary>

```gradle
dependencies {
  implementation platform("software.amazon.awssdk:bom:2.13.26")
  implementation "software.amazon.awssdk:sqs"
  implementation "com.github.headout.ergo-kotlin:ergo-service-sqs:1.2.0"
}
```

</details>

### How to use it?

```kotlin
import headout.oss.ergo.services.SqsMsgService
import headout.oss.ergo.helpers.InMemoryBufferJobResultHandler

val service = SqsMsgService(
    sqsClient,
    REQUEST_QUEUE_URL,
    RESULT_QUEUE_URL,
    numWorkers=20,
    resultHandler=InMemoryBufferJobResultHandler(10)
)

```

#### Existing Result Handlers:

- **[ImmediateRespondJobResultHandler](src/main/kotlin/headout/oss/ergo/helpers/ImmediateRespondJobResultHandler.kt):**
Pushes the result as soon as the job is finished/errored.
It may be SQS request-intensive on the result queue.

- **[InMemoryBufferJobResultHandler](src/main/kotlin/headout/oss/ergo/helpers/InMemoryBufferJobResultHandler.kt):** (default)
Supports batching of job results and pushes in bulk upto 10 messages at a time.

### Architecture

#### SQS Message Schema

- **MessageBody** must be stringified JSON for tasks with atleast one parameter. Key of the JSON will be the parameter name. Example:

```kotlin
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

val sendMsg = SendMessageRequest.builder()
    .messageGroupId("exampleTask")
    .messageBody("{\"i\": 1, \"hi\": \"whatever\"}")
    .queueUrl(REQUEST_QUEUE_URL)
    .build()

// for task with following signature:
@Task("exampleTask")
fun functionTask(i: Int, hi: String): Any
```

- **MessageGroupId** must refer to the **taskId** defined in the consumer client.

> Keep in mind that SQS FIFO queues do not allow receiving messages belonging to same **MessageGroupId** that is being consumed at the moment. So new task requests with same **taskId** can only be picked once all the previous tasks have finished executing.

#### Complete flow with Airflow (SQS as message service)

- [Notes and Sequence Diagram](https://drive.google.com/file/d/1MfT7_k4nEuoxqbdWqhYCKEkye5d8dBeR/view?usp=sharing)
