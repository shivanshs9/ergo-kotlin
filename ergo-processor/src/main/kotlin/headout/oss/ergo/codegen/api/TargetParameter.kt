package headout.oss.ergo.codegen.api

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.TypeName

/**
 * Created by shivanshs9 on 13/08/20.
 */
/**
 * A parameter in user code that should be populated by generated code.
 * */
internal data class TargetParameter(
    val name: String,
    val index: Int,
    val type: TypeName,
    val hasDefault: Boolean,
    val qualifiers: Set<AnnotationSpec>? = null
)