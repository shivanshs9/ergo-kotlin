package headout.oss.ergo

import headout.oss.ergo.annotations.Task

/**
 * Created by shivanshs9 on 05/06/20.
 */
object ExampleTasks {
    @Task("noArg")
    @JvmStatic
    fun noArg(): Boolean = true
}