## Ergo [![Release](https://jitpack.io/v/headout/ergo-kotlin.svg)](https://jitpack.io/#headout/ergo-kotlin) [![Ergo client CI](https://github.com/headout/ergo-kotlin/workflows/Ergo%20client%20CI/badge.svg)](https://github.com/headout/ergo-kotlin/actions?query=workflow%3A"Ergo+client+CI")

### Introduction

Ergo is a client library to run application tasks on a shared pool of workers. It is a generic implementation to handle offloading tasks. "Services" are written to actually receive the message and then the runtime "Job Controller" actually runs the relevant functions annotated with the provided "taskId".

### Installation

#### Add dependencies to [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) (required by the generated code):

<details open>
<summary>Kotlin DSL (build.gradle.kts)</summary>

```kotlin
repositories {
    // artifacts are published to JCenter
    jcenter()
    maven(url="https://jitpack.io")
}

plugins {
  kotlin("plugin.serialization") version kotlinVersion
}

dependencies {
  implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION)) // or "stdlib-jdk8"
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0") // JVM dependency
}
```

</details>

<details>
<summary>Groovy (build.gradle)</summary>

```gradle
repositories {
    // artifacts are published to JCenter
    jcenter()
    maven { url "https://jitpack.io" }
}

plugins {
  id 'org.jetbrains.kotlin.plugin.serialization' version kotlinVersion
}

dependencies {
  implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version" // or "kotlin-stdlib-jdk8"
  implementation "org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0" // JVM dependency
}
```

</details>

#### Add dependencies to Ergo annotation processor:

Add following to Gradle:

<details open>
<summary>Kotlin DSL (build.gradle.kts)</summary>

```kotlin
plugins {
  kotlin("kapt") version kotlinVersion // Enable kapt plugin for annotation processing
}

dependencies {
  kapt("com.github.headout.ergo-kotlin:ergo-processor:1.0.0")
}
```

</details>

<details>
<summary>Groovy (build.gradle)</summary>

```gradle
plugins {
  id 'org.jetbrains.kotlin.kapt' version kotlinVersion // Enable kapt plugin for annotation processing
}

dependencies {
  kapt "com.github.headout.ergo-kotlin:ergo-processor:1.0.0"
}
```

</details>

#### Using SQS queue for receiving tasks

Add following to Gradle:

<details open>
<summary>Kotlin DSL (build.gradle.kts)</summary>

```kotlin
dependencies {
  implementation(platform("software.amazon.awssdk:bom:2.13.26"))
  implementation("software.amazon.awssdk:sqs")
  implementation("com.github.headout.ergo-kotlin:ergo-service-sqs:1.0.0")
}
```

</details>

<details>
<summary>Groovy (build.gradle)</summary>

```gradle
dependencies {
  implementation platform("software.amazon.awssdk:bom:2.13.26")
  implementation "software.amazon.awssdk:sqs"
  implementation "com.github.headout.ergo-kotlin:ergo-service-sqs:1.0.0"
}
```

</details>

#### Optional: Using Spring Services and Autowired properties

Add following to Gradle:

<details open>
<summary>Kotlin DSL (build.gradle.kts)</summary>

```kotlin
dependencies {
  implementation("com.github.headout.ergo-kotlin:ergo-spring:1.0.0")
}
```

</details>

<details>
<summary>Groovy (build.gradle)</summary>

```gradle
dependencies {
  implementation "com.github.headout.ergo-kotlin:ergo-spring:1.0.0"
}
```

</details>

### How to use?

#### Providing Task Metadata in codebase

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

- It even supports suspending functions too! Here's an example:

```kotlin
import headout.oss.ergo.annotations.Task
import kotlinx.coroutines.delay

class ExampleTasks {
    @Task("suspendingTask")
    suspend fun longRunningTask(input: Int): Int {
      delay(2000L)
      return input * input
    }
}
```

#### Running the required service

##### 1. Using SQS Queue for receiving tasks

- Create SQS client and SQS Message Service:

```kotlin
const val AWS_REGION: Region = ...
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

### Architecture

#### Terminology

- Task => used to denote an executable function with given input and given output
  - Has TaskId to uniquely differentiate tasks
  - Can be used to refer to both regular and suspending functions
  - Function Parameters must be serializable (using @Serializable on the data class)
  - The task function can either be regular or suspending function, and its return type is the job result type too (which must again be serializable)
- TaskId => name to map to a particular function (must be unique in project).
  - For SQS FIFO queues, it is analogous to **MessageGroupId**
  - For Pulsar, it is analogous to **Topic**
- JobId => uniquely generated from the sender side to denote a particular running instance of a task.
  - For SQS queues, it is analogous to **MessageId**

#### Message Schema

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

#### Complete flow with Airflow (SQS as message service)

- [Notes and Sequence Diagram](https://drive.google.com/file/d/1MfT7_k4nEuoxqbdWqhYCKEkye5d8dBeR/view?usp=sharing)
