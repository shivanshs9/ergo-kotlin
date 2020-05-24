plugins {
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.serialization") version "1.3.70"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(rootProject.ext["deps.serialization"] as String)
    implementation(rootProject.ext["deps.coroutine"] as String)
    api(project(":ergo-annotations"))

    testImplementation(project(":ergo-processor"))
    testImplementation(rootProject.ext["deps.kotlin-metadata"] as String)
    testImplementation(rootProject.ext["deps.truth"] as String)
    testImplementation(rootProject.ext["deps.junit"] as String)
    testImplementation(rootProject.ext["deps.compile-testing"] as String)
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