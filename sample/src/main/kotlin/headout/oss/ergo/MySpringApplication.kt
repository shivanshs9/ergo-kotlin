package headout.oss.ergo

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * Created by shivanshs9 on 13/07/20.
 */
@SpringBootApplication
open class MySpringApplication {
    companion object {
        fun main(vararg args: String) {
            SpringApplication.run(MySpringApplication::class.java, *args)
        }
    }
}