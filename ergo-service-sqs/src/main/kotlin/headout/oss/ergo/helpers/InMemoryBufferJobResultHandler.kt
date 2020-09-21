package headout.oss.ergo.helpers

import headout.oss.ergo.models.JobResult
import headout.oss.ergo.services.BaseMsgService
import headout.oss.ergo.utils.repeatUntilCancelled
import headout.oss.ergo.utils.ticker
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import mu.KotlinLogging
import org.slf4j.MarkerFactory
import java.util.concurrent.TimeUnit

/**
 * Created by shivanshs9 on 21/09/20.
 */
private val logger = KotlinLogging.logger {}

/**
 * Implements a in-memory buffer for executed job results before attempting to push all buffered
 * results to SQS in bulk.
 *
 * The buffer is grown till [maxResultsToBuffer] size before results are pushed and buffer is cleared.
 * There's also a ticker timeout happening regularly with a delay of [timeoutResultCollect] which when
 * received, pushes the buffered results and clears the buffer regardless of whether the buffer is full.
 *
 * @param maxResultsToBuffer capacity of the buffer beyond which buffer is emptied and pushed (for SQS, recommended value is 10)
 * @param timeoutCollectResult timeout interval (in milliseconds) to push and clear the buffer (default is 2 minutes)
 */
@ExperimentalCoroutinesApi
class InMemoryBufferJobResultHandler(
    private val maxResultsToBuffer: Int,
    private val timeoutCollectResult: Long = TIMEOUT_RESULT_COLLECTION
) : JobResultHandler {
    private lateinit var implPushResults: suspend (List<JobResult<*>>) -> Unit
    private lateinit var timeoutResultCollect: ReceiveChannel<Unit>

    private val bufferedResults = mutableListOf<JobResult<*>>()

    override fun init(scope: CoroutineScope, pushResultsImpl: suspend (List<JobResult<*>>) -> Unit) {
        implPushResults = pushResultsImpl
        timeoutResultCollect = scope.ticker(timeoutCollectResult)
        handleBufferTimeout(scope)
    }

    override suspend fun handleResult(result: JobResult<*>): Boolean {
        addResultToBuffer(result)
        return if (bufferedResults.size == maxResultsToBuffer) {
            pushAndClearBuffer()
            true
        } else false
    }

    private fun handleBufferTimeout(scope: CoroutineScope): Job = scope.launch(Dispatchers.IO) {
        repeatUntilCancelled(BaseMsgService.Companion::collectCaughtExceptions) {
            for (timeoutPing in timeoutResultCollect) {
                logger.debug(MARKER_RESULT_BUFFER, "TIMEOUT: Result Collect!")
                if (bufferedResults.isNotEmpty()) pushAndClearBuffer()
            }
        }
    }

    private suspend fun pushAndClearBuffer() {
        implPushResults(bufferedResults.toList())
        bufferedResults.clear()
    }

    private fun addResultToBuffer(result: JobResult<*>) {
        bufferedResults.add(result)
        logger.debug(MARKER_RESULT_BUFFER) { "Added result of job '${result.jobId}' to buffer (size = ${bufferedResults.size})" }
    }

    companion object {
        private val MARKER_RESULT_BUFFER = MarkerFactory.getMarker("ResultBuffer")

        val TIMEOUT_RESULT_COLLECTION: Long = TimeUnit.MINUTES.toMillis(2)
    }
}