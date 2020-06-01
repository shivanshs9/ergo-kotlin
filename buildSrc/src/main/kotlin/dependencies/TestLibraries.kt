package dependencies

import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * Created by shivanshs9 on 01/06/20.
 */
const val kotlinVersion = "1.3.72"

object TestLibraries {
    private object Versions

    const val compileTesting = "com.github.tschuchortdev:kotlin-compile-testing:1.2.8"
    const val compilerEmbeddable = "org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion"
    const val truth = "com.google.truth:truth:1.0.1"
    const val jUnit = "junit:junit:4.13"
}

fun DependencyHandler.testImplementsCodeGen() {
    add("testImplementation", TestLibraries.compileTesting)
    add("testImplementation", Libraries.kotlinMetadata)
    add("testImplementation", TestLibraries.compilerEmbeddable)
}

fun DependencyHandler.testImplementsCommon() {
    add("testImplementation", TestLibraries.jUnit)
    add("testImplementation", TestLibraries.truth)
}