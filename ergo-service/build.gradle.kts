plugins {
    kotlin("jvm") version "1.3.72"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(rootProject.ext["deps.serialization"] as String)
    implementation(rootProject.ext["deps.coroutine"] as String)

    implementation(project(":ergo-runtime"))
    annotationProcessor(project(":ergo-processor"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

apply(from = rootProject.file("gradle/common.gradle.kts"))