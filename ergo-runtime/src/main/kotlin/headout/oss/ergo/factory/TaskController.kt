package headout.oss.ergo.factory

import headout.oss.ergo.models.JobResult

/**
 * Created by shivanshs9 on 23/05/20.
 */
interface TaskController {
    suspend fun execute(): JobResult<*>
}
