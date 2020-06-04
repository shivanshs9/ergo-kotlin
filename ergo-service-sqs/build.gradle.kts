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

    kaptTest(project(":ergo-processor"))
    testImplementsCommon()
    testImplementsCoroutines()
    testImplementsMock()
}

apply(from = rootProject.file("gradle/common.gradle.kts"))