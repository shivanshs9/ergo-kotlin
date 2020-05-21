package headout.oss.ergo.processors

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import headout.oss.ergo.annotations.Task
import headout.oss.ergo.models.EmptyRequestData
import headout.oss.ergo.models.JobRequest
import headout.oss.ergo.utils.getFromConstructor

/**
 * Created by shivanshs9 on 20/05/20.
 */
class TaskBinder internal constructor(val methodName: String, val task: Task, val method: MethodSignature) {
    private lateinit var requestDataClassName: ClassName

    fun createFunctionSpec(packageName: String): FunSpec = createRequestClassName(packageName).let {
        FunSpec.builder(methodName)
            .addParameter(PARAM_NAME_REQUEST, getRequestType(requestDataClassName))
            .addParameter(PARAM_NAME_CALLBACK, method.callbackType)
            .build()
    }

    fun createRequestDataSpec(): TypeSpec = TypeSpec.classBuilder(requestDataClassName)
        .addModifiers(KModifier.DATA)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameters(method.targetParameters.map { ParameterSpec.get(it.variableElement) })
                .build()
        )
        .addProperties(method.targetParameters.map {
            PropertySpec.getFromConstructor(it.name, it.typeName)
        })
        .build()

    fun isRequestDataNeeded() = method.targetParameters.isNotEmpty()

    private fun createRequestClassName(packageName: String): ClassName =
        (method.targetParameters.takeIf { it.isNotEmpty() }?.let {
            ClassName(packageName, "$PREFIX_CLASS_REQUEST_DATA$methodName")
        } ?: EmptyRequestData::class.asClassName()).also {
            requestDataClassName = it
        }

    private fun getRequestType(type: TypeName): TypeName = JobRequest::class.asTypeName().plusParameter(type)

    class Builder internal constructor(val task: Task) {
        var methodSignature: MethodSignature? = null

        fun build(): TaskBinder = methodSignature.let { signature ->
            if (signature == null) throw IllegalStateException("Method signature not provided with the task")
            val methodName = getTargetMethodName(task.taskId)
            TaskBinder(methodName, task, signature)
        }
    }

    companion object {
        const val PREFIX_CLASS_REQUEST_DATA = "RequestData_"
        const val PARAM_NAME_CALLBACK = "jobCallback"
        const val PARAM_NAME_REQUEST = "jobRequest"

        fun newBuilder(task: Task) = Builder(task)

        fun getTargetMethodName(taskId: String): String {
            return taskId.replace("[.$^#%/:\\-]".toRegex(), "_")
        }
    }
}