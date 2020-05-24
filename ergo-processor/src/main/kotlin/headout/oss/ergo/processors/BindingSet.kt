package headout.oss.ergo.processors

import com.squareup.kotlinpoet.*
import headout.oss.ergo.annotations.Task
import headout.oss.ergo.factory.BaseTaskController
import headout.oss.ergo.models.JobRequestData
import me.eugeniomarletti.kotlin.processing.KotlinProcessingEnvironment
import javax.lang.model.element.*

/**
 * Created by shivanshs9 on 20/05/20.
 */
class BindingSet internal constructor(
    val enclosingElement: TypeElement,
    val targetType: TypeName,
    val bindingClassName: ClassName,
    val isFinal: Boolean,
    val tasks: List<TaskBinder>,
    private val processingEnvironment: KotlinProcessingEnvironment
) {
    fun brewKotlin(): FileSpec = createType().let { type ->
        FileSpec.builder(bindingClassName.packageName, type.name!!)
            .addType(type)
            .apply {
                tasks.forEach {
                    if (it.isRequestDataNeeded()) addType(it.createRequestDataSpec())
                }
            }
            .addComment("Generated code by Ergo. DO NOT MODIFY!!")
            .build()
    }

    private fun createType(): TypeSpec = TypeSpec.classBuilder(bindingClassName.simpleName)
        .addModifiers(KModifier.PUBLIC)
        .addOriginatingElement(enclosingElement)
        .addTypeVariables(createTypeVariables())
        .superclass(BaseTaskController::class)
        .apply {
            if (isFinal) addModifiers(KModifier.FINAL)
            addFunctions(createFunctions())
        }
        .addFunction(overrideCallTaskMethod())
        .build()

    private fun createTypeVariables() = listOf(
        TypeVariableName.invoke(TYPE_ARG_REQUEST, JobRequestData::class.asTypeName()),
        TypeVariableName.invoke(TYPE_ARG_RESULT)
    )

    private fun createFunctions() = tasks.map { it.createFunctionSpec() }

    private fun overrideCallTaskMethod(): FunSpec {

        TODO()
    }

    class Builder internal constructor(
        private val enclosingElement: TypeElement,
        private val isFinal: Boolean,
        private val processingEnvironment: KotlinProcessingEnvironment
    ) {
        private val targetType by lazy { enclosingElement.asClassName() }
        private val taskBuilders: MutableSet<TaskBinder.Builder> = mutableSetOf()

        fun addMethod(task: Task, methodSignature: MethodSignature): Boolean {
            val taskBuilder = TaskBinder.newBuilder(task, targetType).apply {
                this.methodSignature = methodSignature
            }.also { taskBuilders.add(it) }
            return true
        }

        fun build(): BindingSet {
            val tasks = taskBuilders.map { it.build() }
            return BindingSet(
                enclosingElement,
                targetType,
                enclosingElement.getBindingClassName(),
                isFinal,
                tasks,
                processingEnvironment
            )
        }
    }

    companion object {
        const val TYPE_ARG_REQUEST = "Req"
        const val TYPE_ARG_RESULT = "Res"

        fun newBuilder(enclosingElement: TypeElement, processingEnvironment: KotlinProcessingEnvironment): Builder {
            println("CLASS MODIFIERS == ${enclosingElement.modifiers}")
            val isFinal = enclosingElement.modifiers.contains(Modifier.FINAL)
            return Builder(enclosingElement, isFinal, processingEnvironment)
        }
    }
}

private fun TypeElement.getBindingClassName(): ClassName {
    val packageName = packageElement.qualifiedName.toString()
    val className = qualifiedName.toString().substring(packageName.length + 1)
        .replace('.', '$') // since .simpleName is unreliable and sometimes blank
    return ClassName(packageName, "${className}_TaskBinding")
}

val TypeElement.packageElement: PackageElement
    get() {
        var element: Element = this
        while (element.kind != ElementKind.PACKAGE) {
            element = element.enclosingElement
        }
        return element as PackageElement
    }