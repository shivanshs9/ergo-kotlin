plugins {
    kotlin("jvm") version "1.3.72"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":ergo-annotations"))
    implementation("com.google.auto.service:auto-service:1.0-rc7")
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