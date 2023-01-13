package integrationtest

import com.github.shynixn.mccoroutine.minestom.launch
import helper.MockedMinestomServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class MinestomExceptionTest {
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
        val testServer = MockedMinestomServer()
        val logger = Mockito.mock(ComponentLogger::class.java)
        var logMessageCounter = 0
        Mockito.`when`(
            logger.error(Mockito.anyString(), Mockito.any())
        ).thenAnswer {
            logMessageCounter++
        }
        val extension = testServer.boot(logger)
        var actualThreadId = 0L
        var ioThreadId: Long

        // Act
        runBlocking(Dispatchers.IO) {
            ioThreadId = Thread.currentThread().id

            extension.launch {
                throw IllegalArgumentException("UnitTestFailure!")
            }

            extension.launch {
                throw IllegalArgumentException("Another UnitTestFailure!")
            }

            extension.launch {
                actualThreadId = Thread.currentThread().id
            }
        }
        Thread.sleep(500)

        // Assert
        Assertions.assertNotEquals(ioThreadId, actualThreadId)
        Assertions.assertEquals(2, logMessageCounter)
    }
}
