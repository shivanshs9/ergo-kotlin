package headout.oss.ergo.processors

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import headout.oss.ergo.exceptions.ExceptionUtils
import headout.oss.ergo.factory.IJobParser
import headout.oss.ergo.factory.JsonFactory
import headout.oss.ergo.models.EmptyRequestData
import headout.oss.ergo.models.JobResult
import headout.oss.ergo.utils.getExecutableElement
import headout.oss.ergo.utils.tempOverriding
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

/**
 * Created by shivanshs9 on 30/05/20.
 */
class JobParserBinder(val bindingMap: Map<TypeElement, BindingSet>, val elementUtils: Elements) {
    fun brewKotlin() =
        FileSpec.builder(IJobParser::class.java.packageName, CLASS_NAME_JOB_PARSER)
            .addImport("kotlinx.serialization", "serializer")
            .addImport("kotlinx.serialization.builtins", "serializer")
            .addType(createJobRequestParser())
            .addComment("Generated code by Ergo. DO NOT MODIFY!!")
            .build()

    private fun createJobRequestParser() = TypeSpec.objectBuilder(
        CLASS_NAME_JOB_PARSER
    )
        .addSuperinterface(IJobParser::class)
        .addFunction(createParseRequestDataFunction())
        .addFunction(createNewTaskControllerFunction())
        .addFunction(createSerializeJobResultFunction())
        .build()

    private fun createParseRequestDataFunction() = FunSpec.tempOverriding(
        IJobParser::class.getExecutableElement(
            "parseRequestData",
            elementUtils
        )!!
    )
        .beginControlFlow("return when (%N)", "arg0")
        .apply {
            val jsonRef = MemberName(JsonFactory::class.asClassName(), "json")
            val emptyRequestClassName = EmptyRequestData::class.asClassName()
            bindingMap.values.forEach { controllerBind ->
                controllerBind.tasks.forEach { taskBind ->
                    if (taskBind.isRequestDataNeeded()) addStatement(
                        "%S -> %M.parse(%T.serializer(), %N)",
                        taskBind.task.taskId,
                        jsonRef,
                        taskBind.requestDataClassName,
                        "arg1"
                    )
                    else addStatement("%S -> %T", taskBind.task.taskId, emptyRequestClassName)
                }
                addStatement("else -> %T", emptyRequestClassName)
            }
        }
        .endControlFlow()
        .build()

    private fun createNewTaskControllerFunction() = FunSpec.tempOverriding(
        IJobParser::class.getExecutableElement(
            "newTaskController",
            elementUtils
        )!!
    )
        .beginControlFlow("return when (%N)", "arg0")
        .apply {
            bindingMap.values.forEach { controllerBind ->
                val controllerClass = controllerBind.bindingClassName
                controllerBind.tasks.forEach { taskBind ->
                    val requestDataClass = taskBind.requestDataClassName
                    val resultClass = taskBind.method.resultType
                    val paramControllerClass = controllerClass.parameterizedBy(requestDataClass, resultClass)
                    addStatement(
                        "%S -> %T(%N, %N, %N as %T)",
                        taskBind.task.taskId,
                        paramControllerClass,
                        "arg0",
                        "arg1",
                        "arg2",
                        requestDataClass
                    )
                }
            }
            addStatement("else -> %M(%N)", MemberName(ExceptionUtils::class.asClassName(), "taskNotFound"), "arg0")
        }
        .endControlFlow()
        .build()

    private fun createSerializeJobResultFunction() = FunSpec.tempOverriding(
        IJobParser::class.getExecutableElement(
            "serializeJobResult",
            elementUtils
        )!!
    )
        .beginControlFlow("return when (%N.taskId)", "arg0")
        .apply {
            val jsonRef = MemberName(JsonFactory::class.asClassName(), "json")
            val jobResultClass = JobResult::class.asClassName()
            bindingMap.values.forEach { controllerBind ->
                controllerBind.tasks.forEach { taskBind ->
                    val resultClass = taskBind.method.resultType
                    val paramJobResultClass = jobResultClass.parameterizedBy(resultClass)
                    beginControlFlow("%S ->", taskBind.task.taskId)
                    addStatement("val serializer = %T.serializer(%T.serializer())", jobResultClass, resultClass)
                    addStatement("%M.stringify(serializer, %N as %T)", jsonRef, "arg0", paramJobResultClass)
                    endControlFlow()
                }
            }
            addStatement("else -> error(%S)", "Task ID unknown")
        }
        .endControlFlow()
        .build()

    companion object {
        private const val CLASS_NAME_JOB_PARSER = "JobParser"
    }
}