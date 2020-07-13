import dependencies.implementsAwsSqs
import dependencies.implementsCommon
import dependencies.implementsCoroutine
import dependencies.implementsSerialization

plugins {
    kotlinJvm()
    kotlinKapt()
    kotlinxSerialization()
}

dependencies {
    implementsCommon()
    implementsSerialization()
    implementsCoroutine()
    implementsAwsSqs()
    kapt(project(":ergo-processor"))
    implementation(project(":ergo-service-sqs"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
