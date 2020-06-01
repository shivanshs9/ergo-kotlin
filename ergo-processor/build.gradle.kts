import dependencies.*

plugins {
    kotlinJvm()
    kotlinKapt()
}

dependencies {
    implementsCommon()
    implementsSerialization()
    implementsCodeGen()
    implementation(project(":ergo-annotations"))
    implementation(project(":ergo-runtime"))

    testImplementsCommon()
    testImplementsCodeGen()
}

apply(from = rootProject.file("gradle/common.gradle.kts"))