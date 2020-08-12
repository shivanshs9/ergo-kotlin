package headout.oss.ergo.codegen.api

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName

/**
 * Created by shivanshs9 on 13/08/20.
 */
/**
 * A method in user class
 */
internal data class TargetMethod(
    val name: String,
    val returnType: TypeName,
    val parameters: List<TargetParameter>,
    val modifiers: Collection<KModifier>
)