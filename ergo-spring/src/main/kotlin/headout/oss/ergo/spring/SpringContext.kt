package headout.oss.ergo.spring

import headout.oss.ergo.spring.SpringContext.Companion.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

/**
 * Created by shivanshs9 on 13/07/20.
 */

/**
 * Spring [Component] to store the spring's [ApplicationContext] as a static field
 * and provide a [getBean] method to get spring bean instance using the context
 */
@Component
class SpringContext : ApplicationContextAware {
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }

    companion object {
        private lateinit var context: ApplicationContext

        /**
         * Returns a spring bean instance of the given class
         */
        fun <T : Any> getBean(beanClazz: KClass<T>): T = context.getBean(beanClazz.java)
    }
}