package headout.oss.ergo.spring

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

/**
 * Created by shivanshs9 on 13/07/20.
 */
@Component
class SpringContext : ApplicationContextAware {
    init {
        print("SpringContext booted!!")
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        print("SpringContext set context!!")
        context = applicationContext
    }

    companion object {
        private lateinit var context: ApplicationContext

        fun <T : Any> getBean(beanClazz: KClass<T>): T = context.getBean(beanClazz.java)
    }
}