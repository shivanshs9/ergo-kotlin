package headout.oss.ergo.codegen.api

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier

/**
 * Created by shivanshs9 on 13/08/20.
 */
/**
 * A user type that is relevant for generated code.
 */
data class TargetType(
    val className: ClassName,
    val methods: Map<String, TargetMethod>,
    val visibility: KModifier
)