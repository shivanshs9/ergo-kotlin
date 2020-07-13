package headout.oss.ergo

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext

/**
 * Created by shivanshs9 on 13/07/20.
 */
@SpringBootApplication
open class MySpringApplication {
    private lateinit var context: ConfigurableApplicationContext

    fun main(vararg args: String) {
        context = SpringApplication.run(MySpringApplication::class.java, *args)
    }

    fun stop() = context.stop()
}