val VERSION_NAME = "1.1.0"

version = VERSION_NAME
group = "headout.oss"

fun isReleaseBuild() = !VERSION_NAME.contains("SNAPSHOT")
