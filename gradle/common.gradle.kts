val VERSION_NAME = "1.1.5"

version = VERSION_NAME
group = "headout.oss"

fun isReleaseBuild() = !VERSION_NAME.contains("SNAPSHOT")
