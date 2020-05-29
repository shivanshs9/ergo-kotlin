package headout.oss.ergo.services

import headout.oss.ergo.annotations.TaskId

/**
 * Created by shivanshs9 on 29/05/20.
 */
interface TaskServiceConversion {
    fun toTaskId(value: String): TaskId
    fun fromTaskId(taskId: TaskId): String
}