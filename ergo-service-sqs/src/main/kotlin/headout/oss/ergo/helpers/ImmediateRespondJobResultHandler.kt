package headout.oss.ergo.helpers

import headout.oss.ergo.factory.IJobParser
import headout.oss.ergo.models.JobResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by shivanshs9 on 21/09/20.
 */
@ExperimentalCoroutinesApi
class ImmediateRespondJobResultHandler : JobResultHandler {
    private lateinit var implPushResults: suspend (List<JobResult<*>>) -> Unit

    override fun init(
        scope: CoroutineScope,
        jobParser: IJobParser,
        pushResultsImpl: suspend (List<JobResult<*>>) -> Unit
    ) {
        implPushResults = pushResultsImpl
    }

    override suspend fun handleResult(result: JobResult<*>): Boolean {
        implPushResults(listOf(result))
        return true
    }
}