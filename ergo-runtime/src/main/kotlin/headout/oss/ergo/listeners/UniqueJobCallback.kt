package headout.oss.ergo.listeners

import headout.oss.ergo.models.JobResult

/**
 * Created by shivanshs9 on 21/05/20.
 */
class UniqueJobCallback<T>(val jobId: String) : JobCallback<T> {
    override fun success(result: T): JobResult {
        TODO("Not yet implemented")
    }

    override fun error(error: Throwable): JobResult {
        TODO("Not yet implemented")
    }
}