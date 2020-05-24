package headout.oss.ergo.processors

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import headout.oss.ergo.annotations.Task
import headout.oss.ergo.exceptions.ExceptionUtils
import headout.oss.ergo.factory.BaseJobRequestParser
import headout.oss.ergo.factory.JsonFactory
import headout.oss.ergo.models.EmptyRequestData
import headout.oss.ergo.utils.getExecutableElement
import headout.oss.ergo.utils.tempOverriding
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
@AutoService(Processor::class)
class TaskProcessor : KotlinAbstractProcessor() {
    override fun getSupportedAnnotationTypes(): Set<String> = setOf(ANNOTATION_TYPE.canonicalName)

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val bindingMap = processTargets(roundEnv)
        bindingMap.forEach { binding ->
            binding.value.runCatching {
                brewKotlin().writeTo(filer)
            }.onFailure { handleBrewError(it, binding.key) }
        }
        kotlin.runCatching { brewJobRequestKotlin(bindingMap).writeTo(filer) }
            .onFailure { if (it !is FilerException) error(it) }
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

        println("METHOD MODIFIERS == ${element.modifiers}")
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

    private fun brewJobRequestKotlin(bindingMap: Map<TypeElement, BindingSet>) =
        FileSpec.builder(BaseJobRequestParser::class.java.packageName, CLASS_NAME_JOB_REQUEST_PARSER)
            .addImport("kotlinx.serialization", "serializer")
            .addType(createJobRequestParser(bindingMap))
            .addComment("Generated code by Ergo. DO NOT MODIFY!!")
            .build()

    private fun createJobRequestParser(bindingMap: Map<TypeElement, BindingSet>) = TypeSpec.objectBuilder(
        CLASS_NAME_JOB_REQUEST_PARSER
    )
        .superclass(BaseJobRequestParser::class)
        .addFunction(createParseRequestDataFunction(bindingMap))
        .addFunction(createNewTaskControllerFunction(bindingMap))
        .build()

    private fun createParseRequestDataFunction(bindingMap: Map<TypeElement, BindingSet>) = FunSpec.tempOverriding(
        BaseJobRequestParser::class.getExecutableElement(
            "parseRequestData",
            elementUtils
        )!!
    )
        .beginControlFlow("return when (%N)", "arg0")
        .apply {
            val jsonRef = MemberName(JsonFactory::class.asClassName(), "json")
            val emptyRequestClassName = EmptyRequestData::class.asClassName()
            bindingMap.values.forEach { controllerBind ->
                controllerBind.tasks.forEach { taskBind ->
                    if (taskBind.isRequestDataNeeded()) addStatement(
                        "%S -> %M.parse(%T.serializer(), %N)",
                        taskBind.task.taskId,
                        jsonRef,
                        taskBind.requestDataClassName,
                        "arg1"
                    )
                    else addStatement("%S -> %T", taskBind.task.taskId, emptyRequestClassName)
                }
                addStatement("else -> %T", emptyRequestClassName)
            }
        }
        .endControlFlow()
        .build()

    private fun createNewTaskControllerFunction(bindingMap: Map<TypeElement, BindingSet>) = FunSpec.tempOverriding(
        BaseJobRequestParser::class.getExecutableElement(
            "newTaskController",
            elementUtils
        )!!
    )
        .beginControlFlow("return when (%N)", "arg0")
        .apply {
            bindingMap.values.forEach { controllerBind ->
                val controllerClass = controllerBind.bindingClassName
                controllerBind.tasks.forEach { taskBind ->
                    val requestDataClass = taskBind.requestDataClassName
                    val resultClass = taskBind.method.returnType
                    val paramControllerClass = controllerClass.parameterizedBy(requestDataClass, resultClass)
                    addStatement(
                        "%S -> %T(%N, %N, %N as %T)",
                        taskBind.task.taskId,
                        paramControllerClass,
                        "arg0",
                        "arg1",
                        "arg2",
                        requestDataClass
                    )
                }
            }
            addStatement("else -> %M(%N)", MemberName(ExceptionUtils::class.asClassName(), "taskNotFound"), "arg0")
        }
        .endControlFlow()
        .build()

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

        private const val CLASS_NAME_JOB_REQUEST_PARSER = "JobRequestParser"
    }
}

private fun MutableMap<TypeElement, BindingSet.Builder>.attachElement(
    enclosingElement: TypeElement,
    processingEnvironment: KotlinProcessingEnvironment
) =
    getOrPut(enclosingElement) { BindingSet.newBuilder(enclosingElement, processingEnvironment) }