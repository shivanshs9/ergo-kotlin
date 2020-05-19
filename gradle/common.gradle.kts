val VERSION_NAME = "1.0-SNAPSHOT"

version = VERSION_NAME
group = "headout.oss"

fun isReleaseBuild() = !VERSION_NAME.contains("SNAPSHOT")

