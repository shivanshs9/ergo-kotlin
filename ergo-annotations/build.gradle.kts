import dependencies.implementsCommon

plugins {
    kotlinJvm()
}

dependencies {
    implementsCommon()
}

apply(from = rootProject.file("gradle/common.gradle.kts"))