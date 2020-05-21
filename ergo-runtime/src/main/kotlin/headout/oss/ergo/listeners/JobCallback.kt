package headout.oss.ergo.listeners

import headout.oss.ergo.models.JobResult

/**
 * Created by shivanshs9 on 21/05/20.
 */
interface JobCallback<T> {
    fun success(result: T): JobResult
    fun error(error: Throwable): JobResult
}