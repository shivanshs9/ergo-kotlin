package headout.oss.ergo.models

import headout.oss.ergo.annotations.TaskId
import headout.oss.ergo.exceptions.BaseJobError
import headout.oss.ergo.exceptions.ExceptionUtils.getStackTraceWithCause
import kotlinx.serialization.Serializable

/**
 * Created by shivanshs9 on 21/05/20.
 */
@Serializable
data class JobResult<out T>(
    val taskId: TaskId, val jobId: JobId, val data: T? = null,
    val metadata: JobResultMetadata
) : JobParameter {
    val isError by lazy { metadata.status != JobResultMetadata.STATUS.SUCCESS.code }

    companion object {
        fun <T> success(taskId: TaskId, jobId: JobId, data: T) =
            JobResult(taskId, jobId, data, JobResultMetadata.success())

        fun error(taskId: TaskId, jobId: JobId, error: BaseJobError) =
            JobResult<Any>(taskId, jobId, metadata = JobResultMetadata.error(error))
    }
}

@Serializable
data class JobResultMetadata internal constructor(
    val status: Int, val error: ErrorMetadata?
) {
    enum class STATUS(val code: Int) {
        SUCCESS(200),

        ERROR_LIBRARY_JOB_CANCELLED(503),
        ERROR_LIBRARY_INTERNAL(502), ERROR_PARSE(422), ERROR_NOT_FOUND(404),

        ERROR_CLIENT_INTERNAL(500), ERROR_INVALID_REQUEST(400), ERROR_FORBIDDEN(403)
    }

    companion object {
        fun success() = JobResultMetadata(STATUS.SUCCESS.code, null)
        fun <T : BaseJobError> error(error: T) = JobResultMetadata(
            error.status.code,
            ErrorMetadata(error.message ?: error.localizedMessage, error.getStackTraceWithCause())
        )
    }
}

@Serializable
data class ErrorMetadata(val message: String, val traceback: String?)