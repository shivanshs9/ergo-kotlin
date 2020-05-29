package headout.oss.ergo.factory

import headout.oss.ergo.annotations.TaskId
import headout.oss.ergo.exceptions.BaseJobError
import headout.oss.ergo.models.JobId
import headout.oss.ergo.models.JobResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by shivanshs9 on 24/05/20.
 */
abstract class BaseJobController {
    lateinit var requestParser: IJobRequestParser

    private suspend fun createTaskController(taskId: TaskId, jobId: JobId, rawData: String): BaseTaskController<*, *> {
        val requestData = requestParser.parseRequestData(taskId, rawData)
        return requestParser.newTaskController(taskId, jobId, requestData)
    }

    suspend fun runJob(taskId: TaskId, jobId: JobId, rawData: String): JobResult<*> = withContext(Dispatchers.Main) {
        runCatching {
            val controller = createTaskController(taskId, jobId, rawData)
            controller.execute()
        }.getOrElse { exc ->
            JobResult.error(taskId, jobId, exc as BaseJobError)
        }
    }
}