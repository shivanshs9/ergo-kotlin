package headout.oss.ergo.utils

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import me.eugeniomarletti.kotlin.metadata.shadow.name.FqName
import me.eugeniomarletti.kotlin.metadata.shadow.platform.JavaToKotlinClassMap
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.type.TypeVariable
import javax.lang.model.util.Elements
import kotlin.coroutines.Continuation
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

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

fun KClass<*>.getExecutableElement(functionName: String, elements: Elements): ExecutableElement? =
    elements.getAllMembers(elements.getTypeElement(qualifiedName))
        .find { it.kind == ElementKind.METHOD && it.simpleName.contentEquals(functionName) } as? ExecutableElement

fun KClass<*>.getExecutableElement(function: KFunction<*>, elements: Elements): ExecutableElement? =
    getExecutableElement(function.name, elements)

fun TypeSpec.Builder.addSuperclassConstructorParameters(vararg params: CodeBlock) = apply {
    params.forEach { addSuperclassConstructorParameter(it) }
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

/* Temporary hack until https://github.com/square/kotlinpoet/issues/914 and
 * https://github.com/square/kotlinpoet/issues/915 are fixed.
 * TODO: Merge their PRs
 */
fun FunSpec.Companion.tempOverriding(method: ExecutableElement): FunSpec.Builder {
    var modifiers: Set<Modifier> = method.modifiers
    require(
        Modifier.PRIVATE !in modifiers &&
                Modifier.FINAL !in modifiers &&
                Modifier.STATIC !in modifiers
    ) {
        "cannot override method with modifiers: $modifiers"
    }

    val methodName = method.simpleName.toString()
    val funBuilder = FunSpec.builder(methodName)

    funBuilder.addModifiers(KModifier.OVERRIDE)

    modifiers = modifiers.toMutableSet()
    modifiers.remove(Modifier.ABSTRACT)
    funBuilder.jvmModifiers(modifiers)

    method.typeParameters
        .map { it.asType() as TypeVariable }
        .map { it.asTypeVariableName() }
        .forEach { funBuilder.addTypeVariable(it) }

    funBuilder.returns(method.returnType.asTypeName())
    funBuilder.addParameters(method.parameters.map {
        ParameterSpec.builder(it.simpleName.toString(), it.javaToKotlinType()).build()
    })
    val lastIndex = funBuilder.parameters.lastIndex
    if (method.isVarArgs) {
        // TODO: isVarArgs is false for suspending vararg functions
        funBuilder.parameters[lastIndex] = funBuilder.parameters.last()
            .toBuilder()
            .addModifiers(KModifier.VARARG)
            .build()
    } else {
        funBuilder.parameters.last().also {
            if (it.type.belongsToType(Continuation::class)) {
                funBuilder.parameters.removeAt(lastIndex)
                funBuilder.addModifiers(KModifier.SUSPEND)
                var typeArg = (it.type as ParameterizedTypeName).typeArguments[0]
                if (typeArg is WildcardTypeName) typeArg = typeArg.inTypes[0]
                funBuilder.returns(typeArg)
            }
        }
    }

    if (method.thrownTypes.isNotEmpty()) {
        val throwsValueString = method.thrownTypes.joinToString { "%T::class" }
        funBuilder.addAnnotation(
            AnnotationSpec.builder(Throws::class)
                .addMember(throwsValueString, *method.thrownTypes.toTypedArray())
                .build()
        )
    }

    return funBuilder
}