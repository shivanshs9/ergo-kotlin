package headout.oss.ergo.factory

import headout.oss.ergo.spring.SpringContext
import kotlin.reflect.KClass

/**
 * Created by shivanshs9 on 13/07/20.
 */
class SpringInstanceLocator : InstanceLocator {
    override fun <T : Any> getInstance(clazz: KClass<T>): T? =
        kotlin.runCatching { SpringContext.getBean(clazz) }.getOrNull()
}