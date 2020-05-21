package headout.oss.ergo.processors

import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import headout.oss.ergo.listeners.JobCallback
import headout.oss.ergo.models.JobParameter

/**
 * Created by shivanshs9 on 20/05/20.
 */
data class MethodSignature(val name: String, val parameters: List<MethodParameter>, val returnType: TypeName) {
    val callbackType: TypeName by lazy {
        JobCallback::class.asTypeName().plusParameter(returnType)
    }

    val targetParameters by lazy {
        parameters.filter { !it.isSubtypeOf(JobParameter::class) }
    }
}