package headout.oss.ergo.factory

import kotlin.reflect.KClass

/**
 * Created by shivanshs9 on 13/07/20.
 */
interface InstanceLocator {
    fun <T: Any> getInstance(clazz: KClass<T>): T?
}