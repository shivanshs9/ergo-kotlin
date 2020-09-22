# Module ergo-spring

An optional module implementing **InstanceLocator** for spring beans
Just add the package `"headout.oss.ergo.spring"` in `ComponentScan` of your spring application, so this
library caches the spring context on the startup.

#### Add dependencies to Ergo Spring support

Add following to Gradle (build.gradle.kts):

```kotlin
dependencies {
  implementation("com.github.headout.ergo-kotlin:ergo-spring:$ergoVersion")
}
```