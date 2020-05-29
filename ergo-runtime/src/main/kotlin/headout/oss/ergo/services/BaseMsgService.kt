package headout.oss.ergo.services

import headout.oss.ergo.exceptions.LibraryInternalError
import headout.oss.ergo.factory.BaseJobController
import headout.oss.ergo.factory.JobController
import headout.oss.ergo.models.JobResult
import headout.oss.ergo.models.RequestMsg
import headout.oss.ergo.utils.immortalWorkers
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import java.lang.Thread.currentThread
import kotlin.coroutines.CoroutineContext

/**
 * Created by shivanshs9 on 28/05/20.
 */
abstract class BaseMsgService<T>(private val numWorkers: Int = DEFAULT_NUMBER_WORKERS) : CoroutineScope {
    protected val jobController: BaseJobController = JobController

    private val supervisorJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + supervisorJob

    protected open val captures = Channel<MessageCapture<T>>(CAPACITY_CAPTURE_BUFFER)

    fun start() = launch {
        val requests = collectRequests()
        immortalWorkers(numWorkers) { workerId ->
            for (request in requests) {
                val result = runCatching {
                    processRequest(request)
                }.onFailure { exc ->
                    println("Worker '$workerId' on '${currentThread().name}' caught exception trying to process message '${request.jobId}'")
                    exc.printStackTrace()
                }.getOrElse {
                    JobResult.error(
                        request.taskId,
                        request.jobId,
                        LibraryInternalError(
                            it,
                            "Worker '$workerId' on '${currentThread().name}' failed processing message '${request.jobId}'"
                        )
                    )
                }
                captures.send(
                    if (result.isError) ErrorResultCapture(request, result)
                    else SuccessResultCapture(request, result)
                )
                captures.send(RespondResultCapture(request, result))
            }
        }
        handleCaptures()
    }

    fun stop() = supervisorJob.cancel()

    protected abstract suspend fun processRequest(request: RequestMsg<T>): JobResult<*>

    protected abstract suspend fun collectRequests(): ReceiveChannel<RequestMsg<T>>

    protected abstract suspend fun handleCaptures(): Job

    companion object {
        private const val DEFAULT_NUMBER_WORKERS = 8

        const val CAPACITY_CAPTURE_BUFFER = 40
        const val CAPACITY_REQUEST_BUFFER = 20
    }
}

sealed class MessageCapture<T>(val request: RequestMsg<T>)
class PingMessageCapture<T>(request: RequestMsg<T>, val attempt: Int = 1) : MessageCapture<T>(request)
class SuccessResultCapture<T>(request: RequestMsg<T>, val result: JobResult<*>) : MessageCapture<T>(request)
class ErrorResultCapture<T>(request: RequestMsg<T>, val result: JobResult<*>) : MessageCapture<T>(request)
class RespondResultCapture<T>(request: RequestMsg<T>, val result: JobResult<*>) : MessageCapture<T>(request)
