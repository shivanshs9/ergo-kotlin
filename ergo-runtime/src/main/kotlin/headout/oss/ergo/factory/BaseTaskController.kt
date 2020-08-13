package headout.oss.ergo.factory

import headout.oss.ergo.annotations.TaskId
import headout.oss.ergo.exceptions.ExceptionUtils
import headout.oss.ergo.exceptions.ExceptionUtils.handleAndWrapError
import headout.oss.ergo.listeners.JobCallback
import headout.oss.ergo.models.JobId
import headout.oss.ergo.models.JobRequest
import headout.oss.ergo.models.JobRequestData
import headout.oss.ergo.models.JobResult
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Created by shivanshs9 on 21/05/20.
 */
abstract class BaseTaskController<Req : JobRequestData, Res>(
    val taskId: TaskId,
    val jobId: JobId,
    val requestData: Req
) : TaskController {
    // Called by client on success
    open fun processSuccess(result: Res) = JobResult.success(taskId, jobId, result)

    override suspend fun execute(): JobResult<Res> {
        val jobRequest = handleAndWrapError(ExceptionUtils::processLibraryError) { JobRequest(jobId, requestData) }
        return kotlin.runCatching {
            return@runCatching callTask(jobRequest)
        }
            .getOrElse { throw ExceptionUtils.processClientError(it) }
            .let { processSuccess(it) }
    }

    abstract suspend fun callTask(jobRequest: JobRequest<Req>): Res
}