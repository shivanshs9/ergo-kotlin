package headout.oss.ergo.processors

import com.squareup.kotlinpoet.TypeName

/**
 * Created by shivanshs9 on 20/05/20.
 */
data class MethodSignature(val name: String, val parameters: List<MethodParameter>, val returnType: TypeName)