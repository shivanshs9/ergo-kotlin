package headout.oss.ergo.utils

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import me.eugeniomarletti.kotlin.metadata.shadow.name.FqName
import me.eugeniomarletti.kotlin.metadata.shadow.platform.JavaToKotlinClassMap
import javax.lang.model.element.Element
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

fun TypeElement.isTypeEqual(expectedType: String) = this.toString() == expectedType
fun TypeMirror.isTypeEqual(expectedType: String) = this.toString() == expectedType

fun TypeMirror.belongsToType(expectedType: String): Boolean {
    return when {
        isTypeEqual(expectedType) -> true
        this is DeclaredType -> {
            val typeElement = this.asElement() as? TypeElement ?: return false
            if (typeElement.isTypeEqual(expectedType) || typeElement.superclass.belongsToType(expectedType)) return true
            for (interfaceType in typeElement.interfaces) {
                if (interfaceType.belongsToType(expectedType)) return true
            }
            false
        }
        else -> false
    }
}

fun <T : Any> TypeMirror.belongsToType(expectedKlazz: KClass<T>) = belongsToType(expectedKlazz.qualifiedName.toString())

fun PropertySpec.Companion.getFromConstructor(name: String, typeName: TypeName) =
    builder(name, typeName).initializer(name).build()

// Proposed at https://github.com/square/kotlinpoet/issues/236#issuecomment-377784099
fun Element.javaToKotlinType(): TypeName = asType().asTypeName().javaToKotlinType()

fun TypeName.javaToKotlinType(): TypeName = when (this) {
    is ParameterizedTypeName -> {
        (rawType.javaToKotlinType() as ClassName).parameterizedBy(
            *typeArguments.map {
                it.javaToKotlinType()
            }.toTypedArray()
        )
    }
    is WildcardTypeName -> {
        if (inTypes.isNotEmpty()) WildcardTypeName.consumerOf(inTypes[0].javaToKotlinType())
        else WildcardTypeName.producerOf(outTypes[0].javaToKotlinType())
    }

    else -> {
        val className = JavaToKotlinClassMap
            .mapJavaToKotlin(FqName(toString()))?.asSingleFqName()?.asString()
        if (className == null) this
        else ClassName.bestGuess(className)
    }
}