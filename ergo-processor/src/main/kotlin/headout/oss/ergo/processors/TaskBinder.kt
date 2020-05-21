package headout.oss.ergo.processors

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import headout.oss.ergo.annotations.Task
import headout.oss.ergo.listeners.JobCallback
import headout.oss.ergo.utils.belongsToType

/**
 * Created by shivanshs9 on 20/05/20.
 */
class TaskBinder internal constructor(val methodName: String, val task: Task, val method: MethodSignature) {
    fun createFunctionSpec(): FunSpec = FunSpec.builder(methodName)
        .apply {
            if (method.parameters.find { it.type.belongsToType(JobCallback::class) } == null) {
                addParameter(PARAM_NAME_CALLBACK, getCallbackType(method.returnType))
            }
        }
        .addParameters(method.parameters.map { ParameterSpec.get(it.variableElement) })
        .build()

    private fun getCallbackType(type: TypeName): TypeName = JobCallback::class.asTypeName().plusParameter(type)

    class Builder internal constructor(val task: Task) {
        var methodSignature: MethodSignature? = null

        fun build(): TaskBinder = methodSignature.let { signature ->
            if (signature == null) throw IllegalStateException("Method signature not provided with the task")
            val methodName = getTargetMethodName(task.taskId)
            TaskBinder(methodName, task, signature)
        }
    }

    companion object {
        const val PARAM_NAME_CALLBACK = "jobCallback"

        fun newBuilder(task: Task) = Builder(task)

        fun getTargetMethodName(taskId: String): String {
            return taskId.replace("[.$^#%/:\\-]".toRegex(), "_")
        }
    }
}