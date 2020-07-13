import dependencies.*

plugins {
    kotlinJvm()
    kotlinKapt()
    kotlinxSerialization()
}

val springBootVersion = "2.1.0.RELEASE"

dependencies {
    implementsCommon()
    implementsSerialization()
    implementsCoroutine()
    implementsAwsSqs()
    kapt(project(":ergo-processor"))
    implementation(project(":ergo-service-sqs"))
    implementation(project(":ergo-spring"))
    implementsSpring()
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
