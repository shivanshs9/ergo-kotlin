val VERSION_NAME = "1.0.0"

version = VERSION_NAME
group = "com.github.headout"

fun isReleaseBuild() = !VERSION_NAME.contains("SNAPSHOT")
