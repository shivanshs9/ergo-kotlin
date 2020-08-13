package headout.oss.ergo.codegen.task

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import headout.oss.ergo.annotations.Task
import headout.oss.ergo.codegen.api.MethodSignature
import headout.oss.ergo.codegen.api.TargetMethod
import headout.oss.ergo.listeners.JobCallback
import headout.oss.ergo.models.EmptyRequestData
import headout.oss.ergo.models.JobRequest
import headout.oss.ergo.models.JobRequestData
import headout.oss.ergo.utils.getFromConstructor
import kotlinx.serialization.Serializable

/**
 * Created by shivanshs9 on 20/05/20.
 */
class TaskMethodGenerator internal constructor(
    val methodName: String,
    val task: Task,
    val targetMethod: TargetMethod,
    private val targetClassName: ClassName
) {
    val requestDataClassName by lazy {
        if (isRequestDataNeeded()) ClassName(targetClassName.packageName, "$PREFIX_CLASS_REQUEST_DATA$methodName")
        else EmptyRequestData::class.asClassName()
    }

    fun createFunctionSpec(): FunSpec =
        FunSpec.builder(methodName)
            .addParameter(PARAM_NAME_REQUEST, getRequestType(requestDataClassName))
            .addParameter(PARAM_NAME_CALLBACK, method.callbackType)
            .apply {
                val targetArgs = method.getTargetArguments("$PARAM_NAME_REQUEST.requestData.") {
                    when {
                        it.isSubtypeOf(JobRequest::class) -> PARAM_NAME_REQUEST
                        it.isSubtypeOf(JobCallback::class) -> PARAM_NAME_CALLBACK
                        else -> error("No arg found for '${it.name}' (${it.typeName}) in '${method.name}' method")
                    }
                }
                val isRunCatchNeeded = method.callbackParameter == null
                if (isRunCatchNeeded) beginControlFlow("runCatching<%T>", method.returnType)
                val methodReceiver = if (method.isStatic) targetClassName.simpleName else TaskControllerGenerator.PROP_INSTANCE
                addStatement("%L.%L(${targetArgs.joinToString(", ")})", methodReceiver, method.name)
                if (isRunCatchNeeded) {
                    endControlFlow()
                    addStatement(".onSuccess($PARAM_NAME_CALLBACK::success)")
                    addStatement(".onFailure($PARAM_NAME_CALLBACK::error)")
                }
            }
            .build()

    fun createRequestDataSpec(): TypeSpec = TypeSpec.classBuilder(requestDataClassName)
        .addSuperinterface(JobRequestData::class)
        .addAnnotation(Serializable::class)
        .addModifiers(KModifier.DATA)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameters(method.targetParameters.map { ParameterSpec.builder(it.name, it.typeName).build() })
                .build()
        )
        .addProperties(method.targetParameters.map {
            PropertySpec.getFromConstructor(it.name, it.typeName)
        })
        .build()

    fun isRequestDataNeeded() = method.targetParameters.isNotEmpty()

    private fun getRequestType(type: TypeName): TypeName = JobRequest::class.asTypeName().plusParameter(type)

    class Builder internal constructor(
        private val task: Task,
        private val targetMethod: TargetMethod,
        private val enclosingClass: ClassName
    ) {
        fun build(): TaskMethodGenerator {
            val methodName = getTargetMethodName(task.taskId)
            return TaskMethodGenerator(methodName, task, targetMethod, enclosingClass)
        }
    }

    companion object {
        const val PREFIX_CLASS_REQUEST_DATA = "RequestData_"
        const val PARAM_NAME_CALLBACK = "jobCallback"
        const val PARAM_NAME_REQUEST = "jobRequest"

        fun builder(task: Task, targetMethod: TargetMethod, enclosingClass: ClassName) = Builder(task, targetMethod, enclosingClass)

        fun getTargetMethodName(taskId: String): String {
            return taskId.replace("[.$^#%/:\\-]".toRegex(), "_")
        }
    }
}