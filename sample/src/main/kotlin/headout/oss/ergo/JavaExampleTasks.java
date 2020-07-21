package headout.oss.ergo;

import headout.oss.ergo.annotations.Task;

/**
 * Created by shivanshs9 on 22/07/20.
 */
public class JavaExampleTasks {
    @Task(taskId = "noArgWithNoResult")
    static void noArgWithNoResult() {
        System.out.println("long task...");
    }
}
