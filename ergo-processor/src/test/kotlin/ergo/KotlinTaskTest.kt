package ergo

import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import headout.oss.ergo.processors.TaskProcessor
import org.intellij.lang.annotations.Language
import org.junit.Test

/**
 * Created by shivanshs9 on 21/05/20.
 */
class KotlinTaskTest {
    @Test
    fun noArg() {
        val source = """
            package example.tasks
            
            import headout.oss.ergo.annotations.Task
            
            object ExampleTask {
                @Task(taskId="noArg")
                @JvmStatic
                fun noArg(): Boolean = true
            }
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    @Test
    fun oneArg() {
        val source = """
            package example.tasks
            
            import headout.oss.ergo.annotations.Task
            
            object ExampleTask {
                @Task(taskId="oneArg")
                @JvmStatic
                fun square(num: Int) = num * num 
            }
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    @Test
    fun noArgWithJobCallback() {
        val source = """
            package example.tasks
            
            import headout.oss.ergo.annotations.Task
            import headout.oss.ergo.listeners.JobCallback
            
            object ExampleTask {
                @Task(taskId="noArgWithJobCallback")
                @JvmStatic
                fun longProcess(callback: JobCallback<String>) = callback.success("hello world") 
            }
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    @Test
    fun oneArgWithJobCallback() {
        val source = """
            package example.tasks
            
            import headout.oss.ergo.annotations.Task
            import headout.oss.ergo.listeners.JobCallback
            
            object ExampleTask {
                @Task(taskId="oneArgWithJobCallback")
                @JvmStatic
                fun longProcess(num: Int, callback: JobCallback<Int>) = callback.success(num * num) 
            }
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    @Test
    fun noArgWithNonSerializableResult() {
        val source = """
            package example.tasks
            
            import headout.oss.ergo.annotations.Task
            
            object ExampleTask {
                @Task(taskId="noArgWithNonSerializableResult")
                @JvmStatic
                fun noArgWithNonSerializableResult(): Result = Result(10)
            }
            
            data class Result(val number: Int)
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    @Test
    fun noArgWithSerializableResult() {
        val source = """
            package example.tasks
            
            import kotlinx.serialization.Serializable
            import headout.oss.ergo.annotations.Task
            
            object ExampleTask {
                @Task(taskId="noArgWithSerializableResult")
                @JvmStatic
                fun noArgWithSerializableResult(): Result = Result(10)
            }
            
            @Serializable
            data class Result(val number: Int)
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    private fun assertResult(result: KotlinCompilation.Result) {
        result.sourcesGeneratedByAnnotationProcessor.forEach {
            println(it.canonicalPath)
            println(it.readText())
        }
        assertThat(result.exitCode).isEqualTo(ExitCode.OK)
    }

    private fun compile(@Language("kotlin") source: String) = KotlinCompilation().apply {
        sources = listOf(SourceFile.kotlin(EXAMPLE_KOTLIN_FILE, source))
        annotationProcessors = listOf(TaskProcessor())
        inheritClassPath = true
        messageOutputStream = System.out
    }.compile()

    companion object {
        const val EXAMPLE_KOTLIN_FILE = "ExampleTask.kt"
    }
}