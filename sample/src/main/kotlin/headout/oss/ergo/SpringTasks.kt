package headout.oss.ergo

import headout.oss.ergo.annotations.Task
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Created by shivanshs9 on 14/07/20.
 */
@Service
class SpringTasks(
    @Autowired service: SampleService
) {
    @Task("spring_optionalArg")
    fun optionalArg(num: Int?): Boolean = (num ?: 2) % 2 == 0
}