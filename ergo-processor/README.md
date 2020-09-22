# Module ergo-processor

Kapt-enabled module to run annotation processor, on compilation, used to generate required **TaskController** subclasses
for parsing of request data, validation and execution of task 

#### Add dependencies to Ergo annotation processor:

Add following to Gradle (build.gradle.kts):

```kotlin
plugins {
  kotlin("kapt") version kotlinVersion // Enable kapt plugin for annotation processing
}

dependencies {
  kapt("com.github.headout.ergo-kotlin:ergo-processor:$ergoVersion")
}
```
