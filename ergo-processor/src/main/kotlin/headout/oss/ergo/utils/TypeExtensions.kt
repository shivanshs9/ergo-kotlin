package headout.oss.ergo.utils

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.type.TypeVariable
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

val TypeElement.kotlinMetadata: Metadata?
    get() = getAnnotation(Metadata::class.java)

val TypeElement.packageElement: PackageElement
    get() {
        var element: Element = this
        while (element.kind != ElementKind.PACKAGE) {
            element = element.enclosingElement
        }
        return element as PackageElement
    }

fun PropertySpec.Companion.getFromConstructor(name: String, typeName: TypeName) =
    builder(name, typeName).initializer(name).build()

private val VISIBILITY_KMODIFIERS = setOf(
    KModifier.INTERNAL,
    KModifier.PRIVATE,
    KModifier.PROTECTED,
    KModifier.PUBLIC
)

private val VISIBILITY_MODIFIERS_MAP = mapOf(
    Modifier.PRIVATE to KModifier.PRIVATE,
    Modifier.PROTECTED to KModifier.PROTECTED,
    Modifier.PUBLIC to KModifier.PUBLIC
)

fun Collection<KModifier>.visibility(): KModifier = find { it in VISIBILITY_KMODIFIERS } ?: KModifier.PUBLIC

fun Set<Modifier>.visibility(): KModifier =
    find { it in VISIBILITY_MODIFIERS_MAP.keys }?.let { VISIBILITY_MODIFIERS_MAP[it] } ?: KModifier.PUBLIC

fun Collection<FunSpec>.withName(name: String): FunSpec =
    find { it.name == name } ?: error("No method found with name '$name'")

fun TypeSpec.overrideFunction(name: String): FunSpec.Builder = funSpecs.withName(name).toBuilder()
    .addModifiers(KModifier.OVERRIDE)
    .apply {
        modifiers.remove(KModifier.ABSTRACT)
    }

fun TypeSpec.Builder.addSuperclassConstructorParameters(vararg params: String) = apply {
    params.forEach { addSuperclassConstructorParameter(it) }
}

fun TypeSpec.Builder.superclass(className: ClassName, vararg typeArgs: TypeName) = apply {
    superclass(className.parameterizedBy(*typeArgs))
}

fun TypeSpec.Builder.superclass(klazz: KClass<*>, vararg typeArgs: TypeName) = apply {
    superclass(klazz.asClassName(), *typeArgs)
}

fun TypeSpec.Builder.addTypeVariables(vararg typeVars: TypeVariableName) = apply {
    this.typeVariables += typeVars // instead of first converting to list and calling addTypeVariables(Iterable<TypeVariableName>)
}

fun ExecutableElement.toFunSpec(): FunSpec.Builder {
    var modifiers: Set<Modifier> = modifiers

    val methodName = simpleName.toString()
    val funBuilder = FunSpec.builder(methodName)

    funBuilder.addModifiers(KModifier.OVERRIDE)

    modifiers = modifiers.toMutableSet()
    modifiers.remove(Modifier.ABSTRACT)
    funBuilder.jvmModifiers(modifiers)

    typeParameters
        .map { it.asType() as TypeVariable }
        .map { it.asTypeVariableName() }
        .forEach { funBuilder.addTypeVariable(it) }

    funBuilder.returns(returnType.asTypeName())
    funBuilder.addParameters(ParameterSpec.parametersOf(this))
    if (isVarArgs) {
        funBuilder.parameters[funBuilder.parameters.lastIndex] = funBuilder.parameters.last()
            .toBuilder()
            .addModifiers(KModifier.VARARG)
            .build()
    }

    if (thrownTypes.isNotEmpty()) {
        val throwsValueString = thrownTypes.joinToString { "%T::class" }
        funBuilder.addAnnotation(
            AnnotationSpec.builder(Throws::class)
                .addMember(throwsValueString, *thrownTypes.toTypedArray())
                .build()
        )
    }

    return funBuilder
}