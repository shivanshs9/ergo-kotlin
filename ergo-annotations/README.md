## ergo-annotations

Module defining the core annotations required for ergo, namely `@Task`
It is used by the core ergo, in [ergo-runtime](../ergo-runtime), and exposed as API dependency to the client application.

### Providing Task Metadata in codebase

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
