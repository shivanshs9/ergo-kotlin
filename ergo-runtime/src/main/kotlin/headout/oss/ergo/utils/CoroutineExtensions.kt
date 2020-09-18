package headout.oss.ergo.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import mu.KotlinLogging
import java.lang.Thread.currentThread
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Created by shivanshs9 on 28/05/20.
 */
private val logger = KotlinLogging.logger {}

suspend fun CoroutineScope.repeatUntilCancelled(exceptionHandler: (Throwable) -> Unit, block: suspend () -> Unit) {
    while (isActive) {
        try {
            block()
            yield() // To yield the current thread to other coroutines to execute (will otherwise cause starvation)
        } catch (ex: CancellationException) {
            logger.debug(ex) { "coroutine on ${currentThread().name} cancelled" }
        } catch (ex: Exception) {
            logger.warn("${currentThread().name} failed with {$ex}. Retrying...", ex)
            exceptionHandler(ex)
        }
    }
    logger.warn { "coroutine on ${currentThread().name} exiting" }
}

fun CoroutineScope.workers(
    concurrency: Int,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.(workedId: Int) -> Unit
) = (1..concurrency).map { launch(start = start) { block(it) } }

fun CoroutineScope.immortalWorkers(
    concurrency: Int,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    exceptionHandler: (Throwable) -> Unit = {},
    block: suspend CoroutineScope.(workedId: Int) -> Unit
) = workers(concurrency, start) { repeatUntilCancelled(exceptionHandler) { block(it) } }

inline fun <reified E> CoroutineScope.asyncSendDelayed(
    channel: SendChannel<E>,
    element: E,
    delay: Long,
    context: CoroutineContext
) = launch(context) {
    sendDelayed(channel, element, delay)
}

suspend inline fun <reified E> sendDelayed(channel: SendChannel<E>, element: E, delay: Long) {
    delay(delay)
    channel.send(element)
}

@ExperimentalCoroutinesApi
fun CoroutineScope.ticker(
    delayMillis: Long,
    initialDelayMillis: Long = delayMillis,
    context: CoroutineContext = EmptyCoroutineContext
): ReceiveChannel<Unit> {
    require(delayMillis >= 0) { "Expected non-negative delay, but has $delayMillis ms" }
    require(initialDelayMillis >= 0) { "Expected non-negative initial delay, but has $initialDelayMillis ms" }
    return produce(Dispatchers.Unconfined + context, capacity = 0) {
        sendDelayed(channel, Unit, initialDelayMillis)
        while (isActive) sendDelayed(channel, Unit, delayMillis)
    }
}
