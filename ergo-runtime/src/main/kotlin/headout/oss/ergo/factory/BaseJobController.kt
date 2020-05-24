package headout.oss.ergo.factory

import headout.oss.ergo.annotations.TaskId
import headout.oss.ergo.exceptions.BaseJobError
import headout.oss.ergo.models.JobId
import headout.oss.ergo.models.JobResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext

/**
 * Created by shivanshs9 on 24/05/20.
 */
abstract class BaseJobController : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Unconfined

    lateinit var requestParser: JobRequestParser

    private suspend fun createTaskController(taskId: TaskId, jobId: JobId, rawData: String): BaseTaskController<*, *> {
        val requestData = requestParser.getRequestData(taskId, rawData)
        return requestParser.callConstructor(taskId, jobId, requestData)
    }

    suspend fun runJob(taskId: TaskId, jobId: JobId, rawData: String): JobResult<*> {
        return runCatching {
            val controller = createTaskController(taskId, jobId, rawData)
            controller.execute()
        }.let {
            val exc = it.exceptionOrNull()
            it.getOrNull() ?: JobResult.error(jobId, exc as BaseJobError)
        }
    }

    fun launchNewJobAsync(taskId: TaskId, jobId: JobId, rawData: String) = async { runJob(taskId, jobId, rawData) }
}