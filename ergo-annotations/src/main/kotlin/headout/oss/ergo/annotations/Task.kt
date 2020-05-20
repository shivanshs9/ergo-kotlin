package headout.oss.ergo.annotations

/**
 * Created by shivanshs9 on 20/05/20.
 */

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class Task (val taskId: String, val retryOnFail: Boolean = false)