package ergo

import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import headout.oss.ergo.processors.TaskProcessor
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlinx.serialization.compiler.extensions.SerializationComponentRegistrar
import org.junit.Test

/**
 * Created by shivanshs9 on 21/05/20.
 */
class JavaTaskTest {
    @Test
    fun noArg() {
        val source = """
            package example.tasks;
            
            import headout.oss.ergo.annotations.Task;
            
            class ExampleTask {
                @Task(taskId="noArg")
                public static boolean noArg() {
                    return true;
                }
            }
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    @Test
    fun oneArg() {
        val source = """
            package example.tasks;
            
            import headout.oss.ergo.annotations.Task;

            class ExampleTask {
                @Task(taskId="oneArg")
                public static int square(int num) {
                    return num * num;
                }
            }
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    @Test
    fun noArgWithJobCallback() {
        val source = """
            package example.tasks;
            
            import headout.oss.ergo.annotations.Task;
            import headout.oss.ergo.listeners.JobCallback;
            
            class ExampleTask {
                @Task(taskId="noArgWithJobCallback")
                public static void longProcess(JobCallback<String> callback) {
                    callback.success("hello world");
                }
            }
        """.trimIndent()

        val result = compile(source)
        assertResult(result)
    }

    @Test
    fun oneArgWithJobCallback() {
        val source = """
            package example.tasks;
            
            import headout.oss.ergo.annotations.Task;
            import headout.oss.ergo.listeners.JobCallback;
            
            class ExampleTask {
                @Task(taskId="oneArgWithJobCallback")
                public static void longProcess(Integer num, JobCallback<Integer> callback) {
                    callback.success(num * num);
                }
            }
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

    private fun compile(@Language("java") source: String) = KotlinCompilation().apply {
        sources = listOf(SourceFile.java(EXAMPLE_JAVA_FILE, source))
        annotationProcessors = listOf(TaskProcessor())
        inheritClassPath = true
        compilerPlugins = listOf(SerializationComponentRegistrar())
        messageOutputStream = System.out
    }.compile()

    companion object {
        const val EXAMPLE_JAVA_FILE = "ExampleTask.java"
    }
}