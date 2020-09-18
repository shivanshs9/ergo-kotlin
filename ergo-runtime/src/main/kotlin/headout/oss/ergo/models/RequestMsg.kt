package headout.oss.ergo.models

import headout.oss.ergo.annotations.TaskId
import java.util.*

/**
 * Created by shivanshs9 on 29/05/20.
 */
data class RequestMsg<T>(val taskId: TaskId, val jobId: JobId, val message: T) {
    val receiveTimestamp = Date()

    override fun toString(): String {
        return "RequestMsg(taskId=$taskId, jobId=$jobId)"
    }
}