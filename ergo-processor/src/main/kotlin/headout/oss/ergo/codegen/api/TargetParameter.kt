package headout.oss.ergo.codegen.api

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName
import headout.oss.ergo.utils.belongsToType
import kotlin.reflect.KClass

/**
 * Created by shivanshs9 on 13/08/20.
 */
/**
 * A parameter in user code that should be populated by generated code.
 * */
data class TargetParameter(
    val name: String,
    val index: Int,
    val type: TypeName,
    val defaultValue: CodeBlock?,
    val qualifiers: Set<AnnotationSpec>? = null
) {
    fun belongsToType(clazz: KClass<*>) = type.belongsToType(clazz)

    val hasDefault = defaultValue != null
}