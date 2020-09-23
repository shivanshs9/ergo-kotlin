package headout.oss.ergo.helpers

import headout.oss.ergo.factory.IJobParser
import headout.oss.ergo.models.JobResult
import kotlinx.coroutines.CoroutineScope

/**
 * Created by shivanshs9 on 21/09/20.
 */

/**
 * Defines an handler to handle job result after execution
 *
 * It's upto the implementation used in message service to decide when to push the results
 */
interface JobResultHandler {
    fun init(scope: CoroutineScope, jobParser: IJobParser, pushResultsImpl: suspend (List<JobResult<*>>) -> Unit)

    suspend fun handleResult(result: JobResult<*>): Boolean
}