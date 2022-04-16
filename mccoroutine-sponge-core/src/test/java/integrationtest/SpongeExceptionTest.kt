package integrationtest

import com.github.shynixn.mccoroutine.sponge.launch
import helper.MockedSpongeServer
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.logging.Level
import java.util.logging.Logger

class SpongeExceptionTest {
    /**
     * Given
     *  multiple plugin.launch() operations
     * When
     *  2 launches fail and one is successful
     *  The
     *  plugin scope should not fail and the 2 failures be logged.
     */
    @Test
    fun pluginLaunch_MultipleFailingCoroutineScopes_ShouldBeCaughtInRootScopeAndKeepPluginScopeRunning() {
        // Arrange
        val server = MockedSpongeServer()
        val plugin = server.boot()
        val logger = Mockito.mock(Logger::class.java)
        var logMessageCounter = 0
        Mockito.`when`(
            logger.log(
                Mockito.any(Level::class.java),
                Mockito.any(String::class.java),
                Mockito.any(Throwable::class.java)
            )
        ).thenAnswer {
            logMessageCounter++
        }
        var actualThreadId = 0L

        // Act
        runBlocking() {
            plugin.launch {
                throw IllegalArgumentException("UnitTestFailure!")
            }

            plugin.launch {
                throw IllegalArgumentException("Another UnitTestFailure!")
            }

            plugin.launch {
                actualThreadId = Thread.currentThread().id
            }
        }

        Thread.sleep(50)

        // Assert
        Assertions.assertEquals(server.mainThreadId, actualThreadId)
    }
}
