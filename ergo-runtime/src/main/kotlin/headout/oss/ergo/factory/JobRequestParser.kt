package headout.oss.ergo.factory

import headout.oss.ergo.annotations.TaskId
import headout.oss.ergo.models.JobId
import headout.oss.ergo.models.JobRequestData

/**
 * Created by shivanshs9 on 24/05/20.
 */
abstract class JobRequestParser {
    internal abstract suspend fun getRequestData(taskId: TaskId, rawData: String): JobRequestData
//        return when (taskId) {
//            "else" -> JsonFactory.json.parse(X.serializer(), rawData)
//            else -> EmptyRequestData()
//        }

    internal abstract fun callConstructor(
        taskId: TaskId,
        jobId: JobId,
        requestData: JobRequestData
    ): BaseTaskController<*, *>
}