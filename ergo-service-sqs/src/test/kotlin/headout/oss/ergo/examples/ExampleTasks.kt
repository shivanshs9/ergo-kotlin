package headout.oss.ergo.examples

import headout.oss.ergo.annotations.Task
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

/**
 * Created by shivanshs9 on 24/05/20.
 */
object ExampleTasks {
    @Task("xyz.1")
    @JvmStatic
    fun whatever(i: Int, hi: String): Boolean = hi.length == i

    @Task("suspend.2")
    suspend fun happens(request: WhyDisKolaveriDi): Int {
        delay(2000)
        return request.i * 10
    }

    @Task("noArgWithSerializableResult")
    @JvmStatic
    fun noArgWithSerializableResult(): Result =
        Result(10)
}

@Serializable
data class Result(val number: Int)

@Serializable
data class WhyDisKolaveriDi(val i: Int)