package headout.oss.ergo.factory

import headout.oss.ergo.annotations.TaskId
import headout.oss.ergo.models.JobId
import headout.oss.ergo.models.JobRequestData
import headout.oss.ergo.models.JobResult

/**
 * Created by shivanshs9 on 24/05/20.
 */
interface IJobParser {
    suspend fun parseRequestData(taskId: TaskId, rawData: String): JobRequestData

    fun serializeJobResult(jobResult: JobResult<*>): String

    fun deserializeJobResult(taskId: TaskId, rawData: String): JobResult<*>

    fun newTaskController(
        taskId: TaskId,
        jobId: JobId,
        requestData: JobRequestData
    ): BaseTaskController<*, *>

    companion object {
        const val CLASS_NAME_JOB_PARSER = "JobParser"
        val QUALIFIED_NAME_INHERIT_OBJECT = "${IJobParser::class.java.packageName}.$CLASS_NAME_JOB_PARSER"
    }
}