package headout.oss.ergo.processors

import headout.oss.ergo.annotations.Task

/**
 * Created by shivanshs9 on 20/05/20.
 */
class TaskBinder internal constructor(val methodName: String, val task: Task, val method: MethodSignature) {
    class Builder internal constructor(val task: Task) {
        var methodSignature: MethodSignature? = null

        fun build(): TaskBinder = methodSignature.let { signature ->
            if (signature == null) throw IllegalStateException("Method signature not provided with the task")
            val methodName = getTargetMethodName(task.taskId)
            TaskBinder(methodName, task, signature)
        }
    }

    companion object {
        fun newBuilder(task: Task) = Builder(task)

        fun getTargetMethodName(taskId: String): String {
            return taskId.replace("[.$^#%/:\\-]".toRegex(), "_")
        }
    }
}