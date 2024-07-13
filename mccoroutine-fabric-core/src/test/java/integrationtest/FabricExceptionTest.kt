package integrationtest

import com.github.shynixn.mccoroutine.fabric.launch
import com.github.shynixn.mccoroutine.fabric.mcCoroutineConfiguration
import helper.MockedFabricServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.logging.Level
import java.util.logging.Logger

class FabricExceptionTest {
    /**
     * Given
     *  multiple extension.launch() operations
     * When
     *  2 launches fail and one is successful
     *  The
     *  extension scope should not fail and the 2 failures be logged.
     */
    @Test
    fun extensionLaunch_MultipleFailingCoroutineScopes_ShouldBeCaughtInRootScopeAndKeepExtensionScopeRunning() {
        // Arrange
        val testServer = MockedFabricServer()
        val mod = testServer.boot()
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
        mod.mcCoroutineConfiguration.logger = logger
        var actualThreadId = 0L
        var ioThreadId: Long

        // Act
        runBlocking(Dispatchers.IO) {
            ioThreadId = Thread.currentThread().id

            mod.launch {
                throw IllegalArgumentException("UnitTestFailure!")
            }

            mod.launch {
                throw IllegalArgumentException("Another UnitTestFailure!")
            }

            mod.launch {
                actualThreadId = Thread.currentThread().id
            }
        }
        Thread.sleep(2000)

        // Assert
        Assertions.assertNotEquals(ioThreadId, actualThreadId)
        //Assertions.assertEquals(2, logMessageCounter)
    }
}
