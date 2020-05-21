package headout.oss.ergo.processors

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeVariable

/**
 * Created by shivanshs9 on 20/05/20.
 */
data class MethodParameter(val variableElement: VariableElement) {
    val type: TypeName
        get() {
            var parameterType = variableElement.asType()
            if (parameterType is TypeVariable) {
                parameterType = parameterType.upperBound
            }
            return parameterType.asTypeName()
        }
}