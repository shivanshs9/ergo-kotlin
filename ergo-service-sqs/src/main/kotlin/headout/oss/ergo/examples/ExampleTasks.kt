package headout.oss.ergo.examples

import headout.oss.ergo.annotations.Task
import headout.oss.ergo.listeners.JobCallback
import kotlinx.serialization.Serializable

/**
 * Created by shivanshs9 on 24/05/20.
 */
object ExampleTasks {
    @Task("xyz.1")
    @JvmStatic
    fun whatever(i: Int, hi: String): Boolean = hi.length == i

    @Task("abc.2")
    @JvmStatic
    fun happens(request: WhyDisKolaveriDi, callback: JobCallback<Int>) = callback.success(request.i * 10)
}

@Serializable
data class WhyDisKolaveriDi(val i: Int)