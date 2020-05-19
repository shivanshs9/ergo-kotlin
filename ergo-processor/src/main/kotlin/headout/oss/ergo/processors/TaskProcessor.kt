package headout.oss.ergo.processors

import com.google.auto.service.AutoService
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

/**
 * Created by shivanshs9 on 20/05/20.
 */
@AutoService(Processor::class)
class TaskProcessor : AbstractProcessor() {
    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        return true
    }
}