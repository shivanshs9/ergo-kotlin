plugins {
    kotlin("jvm") version "1.3.72"
    kotlin("kapt") version "1.3.72"
    kotlin("plugin.serialization") version "1.3.72"
}

kapt {
    generateStubs = true
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(rootProject.ext["deps.serialization"] as String)
    implementation(rootProject.ext["deps.coroutine"] as String)

    implementation(project(":ergo-runtime"))
    kapt(project(":ergo-processor"))
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