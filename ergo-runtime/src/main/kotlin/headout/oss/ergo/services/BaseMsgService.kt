package headout.oss.ergo.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Created by shivanshs9 on 28/05/20.
 */
abstract class BaseService(private val numWorkers: Int = DEFAULT_NUMBER_WORKERS): CoroutineScope {
    private val supervisorJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + supervisorJob

    fun start() = launch {
        repeat(numWorkers) {
        }
    }

    protected abstract suspend fun CoroutineScope.launchMessageReceiver(channel: SendChannel<Message>)

    fun stop() = supervisorJob.cancel()

    companion object {
        private const val DEFAULT_NUMBER_WORKERS = 8
    }
}

data class Message(val x: Int)