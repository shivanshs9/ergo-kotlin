package headout.oss.ergo

import headout.oss.ergo.annotations.Task
import kotlinx.serialization.Serializable
import java.util.*
import java.util.concurrent.TimeUnit

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
    @Task("suspend_oneArg.1")
    suspend fun longRunning1(num: Int?): Int {
        println("exec longRunning1")
        val defNum = num ?: 4
        val start = Calendar.getInstance().timeInMillis
        while (TimeUnit.MILLISECONDS.toSeconds(Calendar.getInstance().timeInMillis - start) < 100) {
            continue
        }
        if (defNum % 2 == 0) error("send me an odd number") else return defNum * defNum
    }

    @Task("suspend_oneArg.2")
    suspend fun longRunning2(num: Int?): Int {
        println("exec longRunning2")
        val defNum = num ?: 4
        val start = Calendar.getInstance().timeInMillis
        while (TimeUnit.MILLISECONDS.toSeconds(Calendar.getInstance().timeInMillis - start) < 100) {
            continue
        }
        if (defNum % 2 == 0) error("send me an odd number") else return defNum * defNum
    }

    @Task("suspend_oneArg.3")
    suspend fun longRunning3(num: Int?): Int {
        println("exec longRunning3")
        val defNum = num ?: 4
        val start = Calendar.getInstance().timeInMillis
        while (TimeUnit.MILLISECONDS.toSeconds(Calendar.getInstance().timeInMillis - start) < 100) {
            continue
        }
        if (defNum % 2 == 0) error("send me an odd number") else return defNum * defNum
    }
}

@Serializable
data class Result(val result: Int)