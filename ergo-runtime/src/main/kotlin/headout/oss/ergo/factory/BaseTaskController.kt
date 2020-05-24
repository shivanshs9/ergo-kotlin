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
    open fun processSuccess(result: Res) = JobResult.success(jobId, result)

    override suspend fun execute(): JobResult<Res> {
        val jobRequest = handleAndWrapError(ExceptionUtils::processLibraryError) { JobRequest(jobId, requestData) }
        return suspendCoroutine { continuation ->
            val callback = object : JobCallback<Res> {
                override fun success(result: Res) {
                    continuation.resume(processSuccess(result))
                }

                override fun error(error: Throwable) {
                    continuation.resumeWithException(ExceptionUtils.processClientError(error))
                }
            }
            callTask(jobRequest, callback)
        }
    }

    abstract fun callTask(
        jobRequest: JobRequest<Req>,
        jobCallback: JobCallback<Res>
    )
}