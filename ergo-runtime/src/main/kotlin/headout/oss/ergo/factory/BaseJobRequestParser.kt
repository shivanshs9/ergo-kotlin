package headout.oss.ergo.factory

import headout.oss.ergo.annotations.TaskId
import headout.oss.ergo.models.JobId
import headout.oss.ergo.models.JobRequestData

/**
 * Created by shivanshs9 on 24/05/20.
 */
abstract class BaseJobRequestParser {
    abstract suspend fun parseRequestData(taskId: TaskId, rawData: String): JobRequestData

    abstract fun newTaskController(
        taskId: TaskId,
        jobId: JobId,
        requestData: JobRequestData
    ): BaseTaskController<*, *>
}