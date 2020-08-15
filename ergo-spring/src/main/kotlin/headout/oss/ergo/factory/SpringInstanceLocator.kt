package headout.oss.ergo.factory

import headout.oss.ergo.spring.SpringContext
import kotlin.reflect.KClass

/**
 * Created by shivanshs9 on 13/07/20.
 */
class SpringInstanceLocator : InstanceLocator {
    override fun <T : Any> getInstance(clazz: KClass<T>): T? {
        val isSpringBean = clazz.annotations.filter {
            it.annotationClass.qualifiedName?.contains("org.springframework") ?: false
        }.isNotEmpty()
        return if (isSpringBean) SpringContext.getBean(clazz) else null
    }
}