package integrationtest

import com.github.shynixn.mccoroutine.launch
import helper.MockedBukkitServer
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ExceptionTest {


    @Test
    fun doTest() {
        val server = MockedBukkitServer()
        val plugin = server.boot()

        runBlocking {
            println("Unit" + Thread.currentThread().id)

            plugin.launch {
               throw IllegalArgumentException("UnitTestFailure!")
            }

            plugin.launch {
                throw IllegalArgumentException("Another UnitTestFailure!")
            }

            plugin.launch {
                println("Success")
            }

            println("End" + Thread.currentThread().id)
        }

    }
}
