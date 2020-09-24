## ergo-spring

An optional module to integrate Ergo with any Spring application.

Just add the package `"headout.oss.ergo.spring"` in `ComponentScan` of your spring application, so this library caches the spring context on the startup.

### Installation

Add following to Gradle:

<details open>
<summary>Kotlin DSL (build.gradle.kts)</summary>

```kotlin
dependencies {
  implementation("com.github.headout.ergo-kotlin:ergo-spring:1.2.0")
}
```

</details>

<details>
<summary>Groovy (build.gradle)</summary>

```gradle
dependencies {
  implementation "com.github.headout.ergo-kotlin:ergo-spring:1.2.0"
}
```

</details>

### Integrate with existing Spring app

#### Using dispatcher-servlet.xml (for Spring XML config)

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:mvc="http://www.springframework.org/schema/mvc"
	xmlns:sec="http://www.springframework.org/schema/security"
	xmlns:context="http://www.springframework.org/schema/context"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
		http://www.springframework.org/schema/beans/spring-beans-4.3.xsd
		http://www.springframework.org/schema/security
		http://www.springframework.org/schema/security/spring-security-4.2.xsd
		http://www.springframework.org/schema/context
	    http://www.springframework.org/schema/context/spring-context-4.3.xsd
	    http://www.springframework.org/schema/mvc
	    http://www.springframework.org/schema/mvc/spring-mvc-4.3.xsd">

	<context:annotation-config />

	<!-- Support Ergo for Spring -->
	<context:component-scan base-package="headout.oss.ergo.spring" />
	
</beans>
```

#### Using Spring @Configuration (for programmatic Spring configuration)

```java
@Configuration
@EnableWebMvc
@EnableConfigurationProperties
@ComponentScan(basePackages = {
        "headout.oss.ergo.spring", // needed for SpringInstanceLocator to work
})
@ServletComponentScan(basePackageClasses = {AriesCronService.class})
public class ServletConfig extends WebMvcConfigurerAdapter {
}
```
