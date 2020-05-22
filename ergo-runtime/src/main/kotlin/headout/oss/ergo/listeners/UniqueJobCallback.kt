package headout.oss.ergo.listeners

/**
 * Created by shivanshs9 on 21/05/20.
 */
class UniqueJobCallback<T>(val jobId: String) : JobCallback<T> {
    override fun success(result: T) {
        TODO("Not yet implemented")
    }

    override fun error(error: Throwable) {
        TODO("Not yet implemented")
    }
}