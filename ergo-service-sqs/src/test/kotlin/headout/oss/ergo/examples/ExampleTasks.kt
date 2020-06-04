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

//    @Task("noArgWithNonSerializableResult")
//    @JvmStatic
//    fun noArgWithNonSerializableResult(): NonSerializableResult = NonSerializableResult(10)

    @Task("noArgWithSerializableResult")
    @JvmStatic
    fun noArgWithSerializableResult(): Result =
        Result(10)
}

data class NonSerializableResult(val number: Int)

@Serializable
data class Result(val number: Int)

@Serializable
data class WhyDisKolaveriDi(val i: Int)