package headout.oss.ergo.services

import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.QueueAttributeName

/**
 * Created by shivanshs9 on 14/08/20.
 */
suspend fun SqsAsyncClient.getVisibilityTimeout(queueUrl: String): Long {
    val response = getQueueAttributes {
        it.queueUrl(queueUrl)
        it.attributeNames(QueueAttributeName.VISIBILITY_TIMEOUT)
    }.await()
    // the value is in seconds only
    return response.attributes()[QueueAttributeName.VISIBILITY_TIMEOUT]?.toLong() ?: 0
}
