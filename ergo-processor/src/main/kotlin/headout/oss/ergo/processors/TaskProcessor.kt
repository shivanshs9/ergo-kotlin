package headout.oss.ergo.processors

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.asTypeName
import headout.oss.ergo.annotations.Task
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import java.io.IOException
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

/**
 * Created by shivanshs9 on 20/05/20.
 */
@AutoService(Processor::class)
class TaskProcessor : KotlinAbstractProcessor() {
    override fun getSupportedAnnotationTypes(): Set<String> = setOf(ANNOTATION_TYPE.canonicalName)

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val bindingMap = processTargets(roundEnv)
        bindingMap.forEach {binding ->
            binding.value.runCatching {
                brewKotlin().writeTo(filer)
            }.onFailure {
                when (it) {
                    is IOException -> error(it, binding.key, "Unable to write binding for type %s: %s", binding.key, it.localizedMessage)
                    else -> error(it, binding.key, "Encountered unknown error processing %s: %s", binding.key, it.localizedMessage)
                }
            }
        }
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
        val methodName = element.simpleName.toString()
        val annotation = element.getAnnotation(ANNOTATION_TYPE)
        val returnType = element.returnType

        val parameters = element.parameters.map { MethodParameter(it) }

        val methodBinder = MethodSignature(methodName, parameters, returnType.asTypeName())
        val builder = builderMap.attachElement(classElement)
        builder.addMethod(annotation, methodBinder)
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    private fun error(exc: Throwable, element: Element, message: String? = null, vararg args: Any) {
        printMessage(Diagnostic.Kind.ERROR, element, message ?: exc.localizedMessage, args)
    }

    private fun printMessage(
        kind: Diagnostic.Kind,
        element: Element,
        message: String,
        vararg args: Any
    ) {
        val msg = if (args.isNotEmpty()) message.format(args) else message
        messager.printMessage(kind, msg, element)
    }

    companion object {
        val ANNOTATION_TYPE = Task::class.java
    }
}

private fun MutableMap<TypeElement, BindingSet.Builder>.attachElement(enclosingElement: TypeElement) =
    getOrPut(enclosingElement) { BindingSet.newBuilder(enclosingElement) }