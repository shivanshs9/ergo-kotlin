package headout.oss.ergo.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import java.lang.Thread.currentThread
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Created by shivanshs9 on 28/05/20.
 */
suspend fun CoroutineScope.repeatUntilCancelled(exceptionHandler: (Throwable) -> Unit = {}, block: suspend () -> Unit) {
    while (isActive) {
        try {
            block()
            yield() // To yield the current thread to other coroutines to execute (will otherwise cause starvation)
        } catch (ex: CancellationException) {
            println("coroutine on ${currentThread().name} cancelled")
        } catch (ex: Exception) {
            println("${currentThread().name} failed with {$ex}. Retrying...")
            ex.printStackTrace()
            exceptionHandler(ex)
        }
    }
    println("coroutine on ${currentThread().name} exiting")
}

fun CoroutineScope.workers(
    concurrency: Int,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.(workedId: Int) -> Unit
) = (1..concurrency).map { launch(start = start) { block(it) } }

fun CoroutineScope.immortalWorkers(
    concurrency: Int,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.(workedId: Int) -> Unit
) = workers(concurrency, start) { repeatUntilCancelled { block(it) } }

suspend inline fun <reified E> SendChannel<E>.sendDelayed(element: E, delay: Long) = coroutineScope {
    launch {
        delay(delay)
        send(element)
    }
}

fun CoroutineScope.ticker(
    delayMillis: Long,
    initialDelayMillis: Long = delayMillis,
    context: CoroutineContext = EmptyCoroutineContext
): ReceiveChannel<Unit> {
    require(delayMillis >= 0) { "Expected non-negative delay, but has $delayMillis ms" }
    require(initialDelayMillis >= 0) { "Expected non-negative initial delay, but has $initialDelayMillis ms" }
    return produce(Dispatchers.Unconfined + context, capacity = 0) {
        delay(initialDelayMillis)
        channel.send(Unit)
        while (isActive) channel.sendDelayed(Unit, delayMillis)
    }
}