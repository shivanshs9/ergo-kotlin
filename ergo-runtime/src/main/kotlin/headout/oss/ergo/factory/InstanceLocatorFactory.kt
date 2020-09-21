package headout.oss.ergo.factory

import java.util.*
import kotlin.reflect.KClass

/**
 * Created by shivanshs9 on 13/07/20.
 */

/**
 * Uses Java's [ServiceLoader] to try all the implementations of [InstanceLocator]
 * to get the required instance of the given class
 */
object InstanceLocatorFactory : InstanceLocator {
    override fun <T : Any> getInstance(clazz: KClass<T>): T {
        val iter = ServiceLoader.load(InstanceLocator::class.java).iterator()
        return runCatching<T> {
            for (locator in iter) {
                val ret = locator?.getInstance(clazz)
                if (ret != null) return@runCatching ret
            }
            error("No suitable locator found")
        }.getOrElse {
            it.printStackTrace()
            error("Could not instantiate class '${clazz.qualifiedName}'; ${it.localizedMessage}")
        }
    }
}