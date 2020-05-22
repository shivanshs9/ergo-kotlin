package headout.oss.ergo.listeners

import headout.oss.ergo.models.JobParameter

/**
 * Created by shivanshs9 on 21/05/20.
 */
interface JobCallback<T> : JobParameter {
    fun success(result: T)
    fun error(error: Throwable)
}