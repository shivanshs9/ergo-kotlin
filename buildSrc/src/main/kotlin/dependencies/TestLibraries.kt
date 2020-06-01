package dependencies

import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * Created by shivanshs9 on 01/06/20.
 */
object TestLibraries {
    private object Versions {

    }

    const val compileTesting = "com.github.tschuchortdev:kotlin-compile-testing:1.2.8"
    const val truth = "com.google.truth:truth:1.0.1"
    const val jUnit = "junit:junit:4.13"
}

fun DependencyHandler.testImplementsCodeGen() {
    add("testImplementation", TestLibraries.compileTesting)
    add("testImplementation", Libraries.kotlinMetadata)
}

fun DependencyHandler.testImplementsCommon() {
    add("testImplementation", TestLibraries.jUnit)
    add("testImplementation", TestLibraries.truth)
}