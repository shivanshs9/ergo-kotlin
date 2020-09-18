val VERSION_NAME = "1.1.4"

version = VERSION_NAME
group = "headout.oss"

fun isReleaseBuild() = !VERSION_NAME.contains("SNAPSHOT")
