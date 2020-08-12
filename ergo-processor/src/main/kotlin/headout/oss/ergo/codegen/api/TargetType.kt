package headout.oss.ergo.codegen.api

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName

/**
 * Created by shivanshs9 on 13/08/20.
 */
/**
 * A user type that is relevant for generated code.
 */
internal data class TargetType(
    val typeName: TypeName,
    val methods: Map<String, TargetMethod>,
    val visibility: KModifier
)