package headout.oss.ergo.exceptions

import headout.oss.ergo.models.JobResultMetadata.STATUS

/**
 * Created by shivanshs9 on 24/05/20.
 */
abstract class BaseLibraryError(
    status: STATUS,
    error: Throwable? = null,
    msg: String
) : BaseJobError(status, error, msg)

class ParseRequestError(
    error: Throwable? = null,
    msg: String = "Malformed request data, error in deserialization"
) : BaseLibraryError(STATUS.ERROR_PARSE, error, msg)

class TaskNotFoundError(
    error: Throwable? = null,
    msg: String = "Given taskId not found"
) : BaseLibraryError(STATUS.ERROR_NOT_FOUND, error, msg)

class LibraryInternalError(
    error: Throwable,
    msg: String = "Faced unknown error in library logic"
) : BaseLibraryError(STATUS.ERROR_LIBRARY_INTERNAL, error, msg)