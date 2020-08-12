package headout.oss.ergo.codegen.api

import com.squareup.kotlinpoet.TypeName
import headout.oss.ergo.utils.belongsToType
import headout.oss.ergo.utils.javaToKotlinType
import javax.lang.model.element.VariableElement
import kotlin.reflect.KClass

/**
 * Created by shivanshs9 on 20/05/20.
 */
data class MethodParameter(val variableElement: VariableElement) {
    val typeName: TypeName by lazy { variableElement.javaToKotlinType() }

    val name: String by lazy {
        variableElement.simpleName.toString()
    }

    fun isSubtypeOf(classType: KClass<*>) = variableElement.asType().belongsToType(classType)
}