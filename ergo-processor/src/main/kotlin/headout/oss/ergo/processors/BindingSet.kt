package headout.oss.ergo.processors

import com.squareup.kotlinpoet.*
import headout.oss.ergo.annotations.Task
import headout.oss.ergo.factory.BaseTaskController
import javax.lang.model.element.*

/**
 * Created by shivanshs9 on 20/05/20.
 */
class BindingSet internal constructor(
    val enclosingElement: TypeElement,
    val targetType: TypeName,
    val bindingClassName: ClassName,
    val isFinal: Boolean,
    val tasks: List<TaskBinder>
) {
    fun brewKotlin(): FileSpec = createType().let { type ->
        FileSpec.builder(bindingClassName.packageName, type.name!!)
            .addType(type)
            .apply {
                tasks.forEach {
                    if (it.isRequestDataNeeded()) addType(it.createRequestDataSpec())
                }
            }
            .addComment("Generated code by Ergo. DO NOT MODIFY!!")
            .build()
    }

    private fun createType(): TypeSpec = TypeSpec.classBuilder(bindingClassName.simpleName)
        .addModifiers(KModifier.PUBLIC)
        .addOriginatingElement(enclosingElement)
        .superclass(BaseTaskController::class)
        .apply {
            if (isFinal) addModifiers(KModifier.FINAL)
            addFunctions(createFunctions())
        }
        .build()

    private fun createFunctions() = tasks.map { it.createFunctionSpec(bindingClassName.packageName) }

    class Builder internal constructor(
        private val enclosingElement: TypeElement,
        private val targetType: TypeName,
        private val bindingClassName: ClassName,
        private val isFinal: Boolean
    ) {
        private val taskBuilders: MutableSet<TaskBinder.Builder> = mutableSetOf()

        fun addMethod(task: Task, methodSignature: MethodSignature): Boolean {
            val taskBuilder = TaskBinder.newBuilder(task).apply {
                this.methodSignature = methodSignature
            }.also { taskBuilders.add(it) }
            return true
        }

        fun build(): BindingSet {
            val tasks = taskBuilders.map { it.build() }
            return BindingSet(enclosingElement, targetType, bindingClassName, isFinal, tasks)
        }
    }

    companion object {
        fun newBuilder(enclosingElement: TypeElement): Builder {
            var typeName = enclosingElement.asType().asTypeName()
            if (typeName is ParameterizedTypeName) typeName = typeName.rawType

            val bindingClassName = enclosingElement.getBindingClassName()
            val isFinal = enclosingElement.modifiers.contains(Modifier.FINAL)
            return Builder(enclosingElement, typeName, bindingClassName, isFinal)
        }
    }
}

private fun TypeElement.getBindingClassName(): ClassName {
    val packageName = packageElement.qualifiedName.toString()
    val className = qualifiedName.toString().substring(packageName.length)
        .replace('.', '$') // since .simpleName is unreliable and sometimes blank
    return ClassName(packageName, "${className}_TaskBinding")
}

val TypeElement.packageElement: PackageElement
    get() {
        var element: Element = this
        while (element.kind != ElementKind.PACKAGE) {
            element = element.enclosingElement
        }
        return element as PackageElement
    }