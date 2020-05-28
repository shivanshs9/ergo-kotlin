package headout.oss.ergo.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import software.amazon.awssdk.services.sqs.SqsAsyncClient

/**
 * Created by shivanshs9 on 28/05/20.
 */
class SqsService(
    private val sqs: SqsAsyncClient
) : BaseService() {
    override suspend fun CoroutineScope.launchMessageReceiver(channel: SendChannel<Message>) {
        val receiveRequest =
    }
}