package headout.oss.ergo.processors

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.classinspector.elements.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import headout.oss.ergo.annotations.Task
import headout.oss.ergo.codegen.api.CachedClassInspector
import headout.oss.ergo.codegen.api.MethodParameter
import headout.oss.ergo.codegen.api.MethodSignature
import headout.oss.ergo.codegen.task.BindingSet
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import me.eugeniomarletti.kotlin.processing.KotlinProcessingEnvironment
import java.io.IOException
import javax.annotation.processing.FilerException
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.tools.Diagnostic

/**
 * Created by shivanshs9 on 20/05/20.
 */
@KotlinPoetMetadataPreview
@AutoService(Processor::class)
class TaskProcessor : KotlinAbstractProcessor() {
    private val classInspector by lazy {
        CachedClassInspector(
            ElementsClassInspector.create(elementUtils, typeUtils)
        )
    }

    override fun getSupportedAnnotationTypes(): Set<String> = setOf(ANNOTATION_TYPE.canonicalName)

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val bindingMap = processTargets(roundEnv)
        bindingMap.forEach { binding ->
            binding.value.runCatching {
                brewKotlin().writeTo(filer)
            }.onFailure { handleBrewError(it, binding.key) }
        }
        kotlin.runCatching {
            val jobParserBinder = JobParserBinder(bindingMap, this)
            jobParserBinder.brewKotlin().writeTo(filer)
        }.onFailure { if (it !is FilerException) error(it) }
        return false
    }

    private fun processTargets(roundEnv: RoundEnvironment): Map<TypeElement, BindingSet> {
        val builderMap = mutableMapOf<TypeElement, BindingSet.Builder>()
        roundEnv.getElementsAnnotatedWith(ANNOTATION_TYPE).forEach { element ->
            if (element.kind == ElementKind.METHOD && element is ExecutableElement) {
                processMethodBinding(roundEnv, element, builderMap)
            } else messager.printMessage(Diagnostic.Kind.ERROR, "Cannot annotate anything but a method")
        }
        return builderMap.mapValues { it.value.build() }
    }

    private fun processMethodBinding(
        roundEnv: RoundEnvironment,
        element: ExecutableElement,
        builderMap: MutableMap<TypeElement, BindingSet.Builder>
    ) {
        val classElement = (element.enclosingElement as TypeElement)
        val kmClass = classInspector.toImmutableKmClass(classElement)
        kmClass.functions.first { it.name == element.simpleName.toString() }
        val methodName = element.simpleName.toString()
        val annotation = element.getAnnotation(ANNOTATION_TYPE)
        val returnType = element.returnType

        val parameters = element.parameters.map { MethodParameter(it) }

        val methodSignature = MethodSignature(
            methodName,
            parameters,
            returnType.asTypeName(),
            isStatic = element.modifiers.contains(Modifier.STATIC)
        )
        builderMap.attachElement(classElement, this).apply {
            addMethod(annotation, methodSignature)
        }
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    private fun handleBrewError(error: Throwable, element: Element) {
        val msg = when (error) {
            is IOException -> "Unable to write binding for type %s: %s"
            else -> "Encountered unknown error processing %s: %s"
        }
        error(
            error,
            element,
            msg,
            element.toString(),
            error.localizedMessage ?: error.message ?: error.toString()
        )
    }

    private fun error(exc: Throwable, element: Element, message: String? = null, vararg args: Any) {
        exc.printStackTrace()
        printMessage(Diagnostic.Kind.ERROR, element, message ?: exc.localizedMessage ?: exc.toString(), args)
    }

    private fun error(exc: Throwable, message: String? = null, vararg args: Any) {
        exc.printStackTrace()
        printMessage(Diagnostic.Kind.ERROR, null, message ?: exc.localizedMessage ?: exc.toString(), args)
    }

    private fun printMessage(
        kind: Diagnostic.Kind,
        element: Element? = null,
        message: String,
        vararg args: Any
    ) {
        val msg = if (args.isNotEmpty()) message.format(args) else message
        if (element != null) messager.printMessage(kind, msg, element)
        else messager.printMessage(kind, msg)
    }

    companion object {
        val ANNOTATION_TYPE = Task::class.java
    }
}

private fun MutableMap<TypeElement, BindingSet.Builder>.attachElement(
    enclosingElement: TypeElement,
    processingEnvironment: KotlinProcessingEnvironment
) =
    getOrPut(enclosingElement) { BindingSet.newBuilder(enclosingElement, processingEnvironment) }