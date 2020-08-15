package headout.oss.ergo

import headout.oss.ergo.annotations.Task
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

/**
 * Created by shivanshs9 on 05/06/20.
 */
object ExampleTasks {
    @Task("noArg")
    @JvmStatic
    fun noArg(): Boolean = true

    @Task("oneArg")
    @JvmStatic
    fun oneArg(value: Int): Result = Result(value)
}

class InstanceClassTasks {
    @Task("suspend_oneArg")
    suspend fun longRunning(num: Int): Int {
        delay(3000L)
        return num * num
    }
}

@Serializable
data class Result(val result: Int)