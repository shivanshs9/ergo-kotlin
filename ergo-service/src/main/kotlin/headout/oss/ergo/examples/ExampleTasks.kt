package headout.oss.ergo.examples

import headout.oss.ergo.annotations.Task

/**
 * Created by shivanshs9 on 24/05/20.
 */
class ExampleTasks {
    @Task("xyz")
    fun whatever(i: Int, hi: String): Boolean = hi.length == i
}