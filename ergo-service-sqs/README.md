# Module ergo-service-sqs

Module defining SQS Message service to communicate with SQS FIFO queues, using AWS Java SDK v2, to pick up tasks
and push job results
Requires `"software.amazon.awssdk:sqs"` to be added as implementation dependency in your project

#### Add dependencies to Ergo SQS Service:

Add following to Gradle (build.gradle.kts):

```kotlin
dependencies {
  implementation(platform("software.amazon.awssdk:bom:2.13.26"))
  implementation("software.amazon.awssdk:sqs")
  implementation("com.github.headout.ergo-kotlin:ergo-service-sqs:$ergoVersion")
}
```

#### Switch between Result Handlers:
- `headout.oss.ergo.helpers.ImmediateRespondJobResultHandler`: Pushes the result as soon as the job is finished/errored. It may be SQS request-intensive on the result queue.
- `headout.oss.ergo.helpers.InMemoryBufferJobResultHandler`: Supports batching of job results and pushes in bulk upto 10 messages at a time. This result handler is used by default in `SqsMsgService`.