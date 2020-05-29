package headout.oss.ergo.factory

import headout.oss.ergo.annotations.TaskId
import headout.oss.ergo.models.JobId
import headout.oss.ergo.models.JobRequestData

/**
 * Created by shivanshs9 on 24/05/20.
 */
interface IJobRequestParser {
    suspend fun parseRequestData(taskId: TaskId, rawData: String): JobRequestData

    fun newTaskController(
        taskId: TaskId,
        jobId: JobId,
        requestData: JobRequestData
    ): BaseTaskController<*, *>
}