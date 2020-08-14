package headout.oss.ergo.codegen.api

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import headout.oss.ergo.models.JobRequest
import headout.oss.ergo.models.JobResult

/**
 * Created by shivanshs9 on 13/08/20.
 */
/**
 * A method in user class
 */
data class TargetMethod(
    val name: String,
    val returnType: TypeName,
    val parameters: List<TargetParameter>,
    val modifiers: Collection<KModifier>,
    val isStatic: Boolean = false
) {
    val targetParameters by lazy {
        parameters.filter { !it.belongsToType(JobRequest::class) && !it.belongsToType(JobResult::class) }
    }

    val hasOnlyJobRequestParam by lazy {
        parameters.size == 1 && parameters[0].belongsToType(JobRequest::class)
    }
}