allprojects {
    repositories {
        mavenCentral()
    }

    ext {
        set("deps.auto", "com.google.auto.service:auto-service:1.0-rc7")
        set("deps.kotlin-metadata", "me.eugeniomarletti.kotlin.metadata:kotlin-metadata:1.4.0")
        set("deps.kotlinpoet", "com.squareup:kotlinpoet:1.5.0")
    }
}

repositories {
    mavenCentral()
}