package integrationtest

import com.github.shynixn.mccoroutine.velocity.launch
import helper.MockedVelocityServer
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.slf4j.Logger

class VelocityExceptionTest {
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
        val server = MockedVelocityServer()
        val logger = Mockito.mock(Logger::class.java)
        val plugin = server.boot(logger)
        var logMessageCounter = 0
        Mockito.`when`(
            logger.error(Mockito.anyString(),Mockito.any())
        ).thenAnswer {
            logMessageCounter++
        }
        var actualThreadId = 0L

        // Act
        runBlocking(server.ionNettyDispatcher) {
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
        Assertions.assertNotEquals(server.ionNettyDispatcher, actualThreadId)
        Assertions.assertEquals(2, logMessageCounter)
    }
}
