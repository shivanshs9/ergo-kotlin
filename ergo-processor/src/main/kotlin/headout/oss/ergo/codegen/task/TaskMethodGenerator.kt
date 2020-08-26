package headout.oss.ergo.codegen.task

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import headout.oss.ergo.annotations.Task
import headout.oss.ergo.codegen.api.TargetMethod
import headout.oss.ergo.codegen.api.TargetParameter
import headout.oss.ergo.models.EmptyRequestData
import headout.oss.ergo.models.JobRequest
import headout.oss.ergo.models.JobRequestData
import headout.oss.ergo.utils.belongsToType
import headout.oss.ergo.utils.getFromConstructor
import kotlinx.serialization.Serializable

/**
 * Created by shivanshs9 on 20/05/20.
 */
class TaskMethodGenerator internal constructor(
    val methodName: String,
    val task: Task,
    val targetMethod: TargetMethod,
    private val targetClassName: ClassName
) {
    val requestDataClassName: TypeName by lazy {
        when {
            isRequestDataNeeded() -> ClassName(targetClassName.packageName, "$PREFIX_CLASS_REQUEST_DATA$methodName")
            targetMethod.hasOnlyJobRequestParam -> (targetMethod.parameters[0].type as ParameterizedTypeName).typeArguments[0]
            else -> EmptyRequestData::class.asClassName()
        }
    }

    fun createFunctionSpec(genHook: (FunSpec.Builder) -> Unit = { }): FunSpec =
        FunSpec.builder(methodName)
            .addModifiers(KModifier.SUSPEND)
            .addParameter(PARAM_NAME_REQUEST, getRequestType(requestDataClassName))
            .apply {
                genHook(this)
                val targetArgs = targetMethod.getTargetArguments("$PARAM_NAME_REQUEST.requestData.") {
                    when {
                        it.belongsToType(JobRequest::class) -> PARAM_NAME_REQUEST
                        else -> error("No arg found for '${it.name}' (${it.type}) in '${targetMethod.name}' method")
                    }
                }
                val methodReceiver =
                    if (targetMethod.isStatic) targetClassName.simpleName else TaskControllerGenerator.PROP_INSTANCE
                addStatement("return %L.%L(${targetArgs.joinToString(", ")}) as Res", methodReceiver, targetMethod.name)
            }
            .build()

    fun createRequestDataSpec(): TypeSpec = TypeSpec.classBuilder(requestDataClassName as ClassName)
        .addSuperinterface(JobRequestData::class)
        .addAnnotation(Serializable::class)
        .addModifiers(KModifier.DATA)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameters(targetMethod.targetParameters.map {
                    ParameterSpec.builder(it.name, it.type)
                        .apply {
                            if (it.isOptional) {
                                when {
                                    it.hasDefault -> defaultValue(it.defaultValue!!)
                                    else -> defaultValue(CodeBlock.of("null"))
                                }
                            }
                        }
                        .build()
                })
                .build()
        )
        .addProperties(targetMethod.targetParameters.map {
            PropertySpec.getFromConstructor(it.name, it.type)
        })
        .build()

    fun isRequestDataNeeded() = targetMethod.targetParameters.isNotEmpty()

    fun getRequestType(type: TypeName): TypeName =
        if (type.belongsToType(JobRequest::class)) type else JobRequest::class.asTypeName().plusParameter(type)

    class Builder internal constructor(
        private val task: Task,
        private val targetMethod: TargetMethod,
        private val enclosingClass: ClassName
    ) {
        fun build(): TaskMethodGenerator {
            val methodName = getTargetMethodName(task.taskId)
            return TaskMethodGenerator(methodName, task, targetMethod, enclosingClass)
        }
    }

    companion object {
        const val PREFIX_CLASS_REQUEST_DATA = "RequestData_"
        const val PARAM_NAME_REQUEST = "jobRequest"

        fun builder(task: Task, targetMethod: TargetMethod, enclosingClass: ClassName) =
            Builder(task, targetMethod, enclosingClass)

        fun getTargetMethodName(taskId: String): String {
            return taskId.replace("[.$^#%/:\\-]".toRegex(), "_")
        }
    }
}

fun TargetMethod.getTargetArguments(
    prefixTargetArg: String,
    transformer: (TargetParameter) -> String
): Iterable<String> =
    parameters.map {
        if (targetParameters.contains(it)) "${prefixTargetArg}${it.name}"
        else transformer(it)
    }