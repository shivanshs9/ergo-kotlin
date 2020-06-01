import dependencies.*

plugins {
    kotlinJvm()
    kotlinKapt()
    kotlinxSerialization()
}

kapt {
    generateStubs = true
}

dependencies {
    implementsCommon()
    implementsSerialization()
    implementsCoroutine()
    implementation(Libraries.coroutineJdk8)
    implementsAwsSqs()
    implementation(project(":ergo-runtime"))
    kapt(project(":ergo-processor"))
}

apply(from = rootProject.file("gradle/common.gradle.kts"))