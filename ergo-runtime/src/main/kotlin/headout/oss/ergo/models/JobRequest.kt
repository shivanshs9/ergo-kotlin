package headout.oss.ergo.models

/**
 * Created by shivanshs9 on 21/05/20.
 */
data class JobRequest<T>(val jobId: JobId, val requestData: T) : JobParameter

typealias JobId = String