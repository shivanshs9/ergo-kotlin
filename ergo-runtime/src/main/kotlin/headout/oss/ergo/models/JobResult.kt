package headout.oss.ergo.models

import headout.oss.ergo.exceptions.BaseJobError
import kotlinx.serialization.Serializable

/**
 * Created by shivanshs9 on 21/05/20.
 */
@Serializable
data class JobResult<out T>(
    val jobId: JobId, val data: T? = null,
    val metadata: JobResultMetadata
) : JobParameter {
    companion object {
        fun <T> success(jobId: JobId, data: T) = JobResult(jobId, data, JobResultMetadata.success())
        fun error(jobId: JobId, error: BaseJobError) = JobResult<Any>(jobId, metadata = JobResultMetadata.error(error))
    }
}

@Serializable
data class JobResultMetadata internal constructor(
    val status: Int, val errorMsg: String? = null
) {
    enum class STATUS(val code: Int) {
        SUCCESS(200),

        ERROR_LIBRARY_INTERNAL(502), ERROR_PARSE(422), ERROR_NOT_FOUND(404),

        ERROR_CLIENT_INTERNAL(500), ERROR_INVALID_REQUEST(400), ERROR_FORBIDDEN(403)
    }

    companion object {
        fun success() = JobResultMetadata(STATUS.SUCCESS.code)
        fun <T : BaseJobError> error(error: T) = JobResultMetadata(error.status.code, error.message)
    }
}
