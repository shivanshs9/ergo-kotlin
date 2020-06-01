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

    // Provides serialization compiler plugin for compiler-testing
    testImplementation("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
}

apply(from = rootProject.file("gradle/common.gradle.kts"))