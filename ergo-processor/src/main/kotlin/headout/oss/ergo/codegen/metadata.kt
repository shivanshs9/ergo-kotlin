package headout.oss.ergo.codegen

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import headout.oss.ergo.codegen.api.CachedClassInspector
import headout.oss.ergo.codegen.api.TargetMethod
import headout.oss.ergo.codegen.api.TargetParameter
import headout.oss.ergo.codegen.api.TargetType
import headout.oss.ergo.utils.visibility
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/**
 * Created by shivanshs9 on 13/08/20.
 */
private fun KotlinAbstractProcessor.javaTargetType(
    element: TypeElement
): TargetType {
    val methods = elementUtils.getAllMembers(element)
        .filter { it.kind == ElementKind.METHOD }
        .map { elem ->
            val method = elem as ExecutableElement
            val funSpec = FunSpec.overriding(method).apply {
                modifiers.remove(KModifier.OVERRIDE)
            }
            TargetMethod(
                name = method.simpleName.toString(),
                returnType = method.returnType.asTypeName(),
                modifiers = funSpec.modifiers,
                parameters = funSpec.parameters.toTargetParameters()
            )
        }.associateBy { it.name }
    return TargetType(
        typeName = element.asClassName(),
        methods = methods,
        visibility = element.modifiers.visibility()
    )
}

@KotlinPoetMetadataPreview
internal fun KotlinAbstractProcessor.targetType(
    element: TypeElement,
    classInspector: CachedClassInspector
): TargetType? {
    return classInspector.toImmutableKmClass(element)?.let { kmClass ->
        val kotlinApi = classInspector.toTypeSpec(kmClass)
        val methods = kotlinApi.funSpecs.map { funSpec ->
            TargetMethod(
                name = funSpec.name,
                returnType = funSpec.returnType!!,
                modifiers = funSpec.modifiers,
                parameters = funSpec.parameters.toTargetParameters()
            )
        }.associateBy { it.name }
        return TargetType(
            typeName = ClassInspectorUtil.createClassName(kmClass.name),
            methods = methods,
            visibility = kotlinApi.modifiers.visibility()
        )
    } ?: javaTargetType(element)
}

private fun Collection<ParameterSpec>.toTargetParameters() = mapIndexed { index, parameterSpec ->
    TargetParameter(
        name = parameterSpec.name,
        index = index,
        type = parameterSpec.type,
        hasDefault = parameterSpec.defaultValue != null
    )
}
