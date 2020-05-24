package headout.oss.ergo.exceptions

import headout.oss.ergo.models.JobResultMetadata

/**
 * Created by shivanshs9 on 24/05/20.
 */
abstract class BaseJobError(
    val status: JobResultMetadata.STATUS,
    error: Throwable? = null,
    msg: String? = null
) : Throwable(msg, error)