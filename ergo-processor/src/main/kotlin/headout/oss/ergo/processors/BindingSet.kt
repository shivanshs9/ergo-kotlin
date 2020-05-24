package headout.oss.ergo.processors

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import headout.oss.ergo.annotations.Task
import headout.oss.ergo.annotations.TaskId
import headout.oss.ergo.exceptions.ExceptionUtils
import headout.oss.ergo.factory.BaseTaskController
import headout.oss.ergo.listeners.JobCallback
import headout.oss.ergo.models.JobId
import headout.oss.ergo.models.JobRequest
import headout.oss.ergo.models.JobRequestData
import headout.oss.ergo.utils.addSuperclassConstructorParameters
import headout.oss.ergo.utils.addTypeVariables
import headout.oss.ergo.utils.getExecutableElement
import headout.oss.ergo.utils.superclass
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
    private val classTypeVariables by lazy {
        arrayOf(
            TypeVariableName.invoke(TYPE_ARG_REQUEST, JobRequestData::class.asTypeName()),
            TypeVariableName.invoke(TYPE_ARG_RESULT)
        )
    }

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
        .addTypeVariables(*classTypeVariables)
        .superclass(BaseTaskController::class, *classTypeVariables)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(ParameterSpec(PARAM_TASKID, TaskId::class.asTypeName()))
                .addParameter(ParameterSpec(PARAM_JOBID, JobId::class.asTypeName()))
                .addParameter(ParameterSpec(PARAM_REQUESTDATA, classTypeVariables[0]))
                .build()
        )
        .addSuperclassConstructorParameters(PARAM_TASKID, PARAM_JOBID, PARAM_REQUESTDATA)
        .apply {
            if (isFinal) addModifiers(KModifier.FINAL)
            addFunctions(createFunctions())
        }
        .addFunction(overrideCallTaskMethod())
        .build()

    private fun createFunctions() = tasks.map { it.createFunctionSpec() }

    private fun overrideCallTaskMethod(): FunSpec = FunSpec.overriding(
        BaseTaskController::class.getExecutableElement(
            "callTask",
            processingEnvironment.elementUtils
        )!!
    )
        .beginControlFlow("return when (taskId)")
        .apply {
            val jobRequestClass = JobRequest::class.asClassName()
            val jobCallbackClass = JobCallback::class.asClassName()
            tasks.forEach {
                addStatement(
                    "%S -> %N(%N as %T, %N as %T)",
                    it.task.taskId,
                    it.methodName,
                    "arg0",
                    jobRequestClass.parameterizedBy(it.requestDataClassName),
                    "arg1",
                    jobCallbackClass.parameterizedBy(it.method.returnType)
                )
            }
            addStatement("else -> %M($PARAM_TASKID)", MemberName(ExceptionUtils::class.asClassName(), "taskNotFound"))
        }
        .endControlFlow()
        .build()

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

        const val PARAM_TASKID = "taskId"
        const val PARAM_JOBID = "jobId"
        const val PARAM_REQUESTDATA = "requestData"

        const val ARG_JOB_REQUEST = "jobRequest"
        const val ARG_JOB_CALLBACK = "jobCallback"

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