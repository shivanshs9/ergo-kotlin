## ergo-processor

Kapt-enabled module to run annotation processor, on compilation, used to generate required **TaskController** subclasses
for parsing of request data, validation and execution of task 

### Installation

Add following to Gradle:

<details open>
<summary>Kotlin DSL (build.gradle.kts)</summary>

```kotlin
plugins {
  kotlin("kapt") version kotlinVersion // Enable kapt plugin for annotation processing
}

dependencies {
  kapt("com.github.headout.ergo-kotlin:ergo-processor:1.2.0")
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
  kapt "com.github.headout.ergo-kotlin:ergo-processor:1.2.0"
}
```

</details>
