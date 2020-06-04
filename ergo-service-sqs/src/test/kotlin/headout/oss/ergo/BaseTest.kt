package headout.oss.ergo

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass

/**
 * Created by shivanshs9 on 02/06/20.
 */
@ExperimentalCoroutinesApi
open class BaseTest {
    protected val testScope = TestCoroutineScope() + SupervisorJob()

    init {
        MockKAnnotations.init(this)
    }

    @Before
    open fun beforeTest() {
        println("beforeTest")
    }

    @After
    open fun afterTest() {
        println("afterTest")
        clearAllMocks()
    }

    companion object {
        @BeforeClass
        @JvmStatic
        fun setupTestCases() {
            Dispatchers.setMain(TestCoroutineDispatcher())
        }

        @AfterClass
        @JvmStatic
        fun cleanupTestCases() {
            Dispatchers.resetMain()
            unmockkAll()
        }
    }
}