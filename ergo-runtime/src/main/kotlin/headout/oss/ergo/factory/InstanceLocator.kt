package headout.oss.ergo.factory

import kotlin.reflect.KClass

/**
 * Created by shivanshs9 on 13/07/20.
 */

/**
 * Defines an interface to return instance of a given class
 * Used by Ergo service to get the task's enclosing class's instance
 * to call the task in runtime
 *
 * [TaskController] gets the instance using the [InstanceLocatorFactory]
 * to locate a suitable instance for the required class
 */
interface InstanceLocator {
    fun <T : Any> getInstance(clazz: KClass<T>): T?
}