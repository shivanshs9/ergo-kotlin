package headout.oss.ergo.factory

import headout.oss.ergo.annotations.TaskId
import headout.oss.ergo.models.JobId
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Created by shivanshs9 on 24/05/20.
 */
object JobController : BaseJobController() {
    suspend fun launchNewJobAsync(taskId: TaskId, jobId: JobId, rawData: String) =
        coroutineScope { async { runJob(taskId, jobId, rawData) } }
}