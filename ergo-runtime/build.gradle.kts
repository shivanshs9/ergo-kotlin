import dependencies.*
import publish.GithubPackage

plugins {
    kotlinJvm()
    kotlinDoc()
    kotlinxSerialization()
}

dependencies {
    implementsCommon()
    implementsSerialization()
    implementsCoroutine()
    implementsReflection()
    api(project(":ergo-annotations"))

    testImplementation(project(":ergo-processor"))
    testImplementsCommon()
    testImplementsCodeGen()
}

publishing {
    GithubPackage(project)
}

apply(from = rootProject.file("gradle/common.gradle.kts"))