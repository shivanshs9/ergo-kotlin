plugins {
    kotlin("jvm") version "1.3.72"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":ergo-annotations"))
    implementation(project(":ergo"))
    implementation(rootProject.ext["deps.auto"] as String)
    annotationProcessor(rootProject.ext["deps.auto"] as String)
    implementation(rootProject.ext["deps.kotlin-metadata"] as String)
    implementation(rootProject.ext["deps.kotlinpoet"] as String)
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