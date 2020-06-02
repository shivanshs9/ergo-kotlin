package dependencies

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.kotlin

/**
 * Created by shivanshs9 on 01/06/20.
 */
object Libraries {
    private object Versions {
        const val kotlinPoet = "1.5.0"
        const val coroutine = "1.3.7"
        const val aws2 = "2.13.26"
    }

    const val autoService = "com.google.auto.service:auto-service:1.0-rc7"
    const val kotlinMetadata = "me.eugeniomarletti.kotlin.metadata:kotlin-metadata:1.4.0"
    const val kotlinPoetCore = "com.squareup:kotlinpoet:${Versions.kotlinPoet}"
    const val kotlinPoetMetadata = "com.squareup:kotlinpoet-metadata:${Versions.kotlinPoet}"
    const val serializationRuntime = "org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0"
    const val coroutine = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutine}"
    const val coroutineJdk8 = "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.coroutine}"
    const val aws2Bom = "software.amazon.awssdk:bom:${Versions.aws2}"
    const val aws2Sqs = "software.amazon.awssdk:sqs"
}

fun DependencyHandler.implementsKotlinPoet() {
    add("implementation", Libraries.kotlinMetadata)
    add("implementation", Libraries.kotlinPoetMetadata)
    add("implementation", Libraries.kotlinPoetCore)
}

fun DependencyHandler.implementsCodeGen() {
    add("implementation", Libraries.autoService)
    add("kapt", Libraries.autoService)
    implementsKotlinPoet()
}

fun DependencyHandler.implementsSerialization() {
    add("implementation", Libraries.serializationRuntime)
}

fun DependencyHandler.implementsCommon() {
    add("implementation", kotlin("stdlib-jdk8"))
}

fun DependencyHandler.implementsCoroutine() {
    add("implementation", Libraries.coroutine)
}

fun DependencyHandler.implementsAwsSqs() {
    add("implementation", platform(Libraries.aws2Bom))
    add("implementation", Libraries.aws2Sqs)
}