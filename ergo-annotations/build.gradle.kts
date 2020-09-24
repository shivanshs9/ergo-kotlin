import dependencies.implementsCommon
import publish.GithubPackage

plugins {
    kotlinJvm()
    kotlinDoc()
}

dependencies {
    implementsCommon()
}

publishing {
    GithubPackage(project)
}

apply(from = rootProject.file("gradle/common.gradle.kts"))