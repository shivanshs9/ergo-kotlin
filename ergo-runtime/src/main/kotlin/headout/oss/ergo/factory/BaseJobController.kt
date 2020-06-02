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
    val parser: IJobParser by lazy {
        Class.forName(IJobParser.QUALIFIED_NAME_INHERIT_OBJECT).kotlin.objectInstance as IJobParser
    }

    private suspend fun createTaskController(taskId: TaskId, jobId: JobId, rawData: String): BaseTaskController<*, *> {
        val requestData = parser.parseRequestData(taskId, rawData)
        return parser.newTaskController(taskId, jobId, requestData)
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