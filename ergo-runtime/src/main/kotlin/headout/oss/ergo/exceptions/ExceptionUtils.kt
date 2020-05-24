package headout.oss.ergo.exceptions

/**
 * Created by shivanshs9 on 24/05/20.
 */
object ExceptionUtils {
    // Called by client on error
    fun processClientError(error: Throwable): BaseJobError = when (error) {
        // TODO: Handle more errors
        else -> ClientInternalError(error)
    }

    // Called by library on error
    fun processLibraryError(error: Throwable): BaseLibraryError = when (error) {
        // TODO: Handle more errors
        else -> LibraryInternalError(error)
    }

    fun <T : Any> handleAndWrapError(errorHandler: (Throwable) -> Throwable, block: () -> T): T {
        return try {
            block()
        } catch (exc: Throwable) {
            throw errorHandler(exc)
        }
    }
}