package headout.oss.ergo.annotations

/**
 * Created by shivanshs9 on 20/05/20.
 */

/**
 * Task annotation to use on any function denoting it as Ergo Task
 * with the given [taskId]. All functions annotated with this are processed
 * and generated on compile-time, validating the request arguments.
 *
 * It can be used for both regular and suspending functions.
 * Ensure that all the parameters and return type are serializable (using [Serializable] annotation on data class)
 *
 * @param taskId unique identifier for the given function associated with a task
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class Task(val taskId: TaskId, val retryOnFail: Boolean = false)

typealias TaskId = String