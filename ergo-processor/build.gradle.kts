plugins {
    kotlin("jvm") version "1.3.72"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":ergo-annotations"))
    implementation(project(":ergo-runtime"))
    implementation(rootProject.ext["deps.auto"] as String)
    annotationProcessor(rootProject.ext["deps.auto"] as String)
    implementation(rootProject.ext["deps.kotlin-metadata"] as String)
    implementation(rootProject.ext["deps.kotlinpoet-metadata"] as String)
    implementation(rootProject.ext["deps.kotlinpoet"] as String)

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