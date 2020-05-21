package headout.oss.ergo.utils

import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

/**
 * Created by shivanshs9 on 21/05/20.
 */
fun TypeName.belongsToType(expectedType: TypeName): Boolean {
    return if (this is ParameterizedTypeName) rawType == expectedType else this == expectedType
}

fun <T : Any> TypeName.belongsToType(expectedKlazz: KClass<T>) = belongsToType(expectedKlazz.asTypeName())

fun TypeMirror.isTypeEqual(expectedType: String) = this.toString() == expectedType

fun TypeMirror.belongsToType(expectedType: String): Boolean {
    return when {
        isTypeEqual(expectedType) -> true
        this is DeclaredType -> {
            val typeElement = this.asElement() as? TypeElement ?: return false
            if (typeElement.superclass.belongsToType(expectedType)) return true
            for (interfaceType in typeElement.interfaces) {
                if (interfaceType.belongsToType(expectedType)) return true
            }
            false
        }
        else -> false
    }
}

fun <T : Any> TypeMirror.belongsToType(expectedKlazz: KClass<T>) = belongsToType(expectedKlazz.simpleName.toString())

fun PropertySpec.Companion.getFromConstructor(name: String, typeName: TypeName) =
    builder(name, typeName).initializer(name).build()