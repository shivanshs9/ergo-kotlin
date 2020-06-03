package headout.oss.ergo.exceptions

import headout.oss.ergo.models.JobResultMetadata.STATUS

/**
 * Created by shivanshs9 on 24/05/20.
 */
abstract class BaseClientError(
    status: STATUS,
    error: Throwable? = null,
    msg: String
) : BaseJobError(status, error, msg)

class InvalidRequestError(
    error: Throwable? = null,
    msg: String = "Request data is invalid!"
) : BaseClientError(STATUS.ERROR_INVALID_REQUEST, error, msg)

class TaskNotFoundError(
    error: Throwable? = null,
    msg: String = "Given taskId not found"
) : BaseLibraryError(STATUS.ERROR_NOT_FOUND, error, msg)

class ClientInternalError(
    error: Throwable,
    msg: String = "Faced unknown error in client logic"
) : BaseClientError(STATUS.ERROR_CLIENT_INTERNAL, error, msg)