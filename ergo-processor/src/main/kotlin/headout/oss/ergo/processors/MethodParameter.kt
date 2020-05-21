package headout.oss.ergo.processors

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import headout.oss.ergo.utils.belongsToType
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeVariable
import kotlin.reflect.KClass

/**
 * Created by shivanshs9 on 20/05/20.
 */
data class MethodParameter(val variableElement: VariableElement) {
    val typeName: TypeName by lazy {
        var parameterType = variableElement.asType()
        if (parameterType is TypeVariable) {
            parameterType = parameterType.upperBound
        }
        parameterType.asTypeName()
    }

    val name: String by lazy {
        variableElement.simpleName.toString()
    }

    fun isSubtypeOf(classType: KClass<*>) = variableElement.asType().belongsToType(classType)
}