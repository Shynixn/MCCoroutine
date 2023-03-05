package integrationtest

import com.github.shynixn.mccoroutine.minestom.setSuspendingDefaultExecutor
import helper.MockedMinestomServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.extensions.Extension
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MinestomCommandTest {
    /**
     *  Given
     *  a call of a simple suspending command
     *  When
     *  executeServerCommand is called from any context
     *  Then
     *  the command should be called on the correct threads.
     */
    @Test
    fun dispatchCommand_SimpleSuspendingCommandExecutor_ShouldCallOnCorrectThreads() {
        val server = MockedMinestomServer()
        val extension = server.boot()
        var unitTestThreadId: Long
        val testCommandExecutor = TestCommandExecutor(extension)

        runBlocking {
            unitTestThreadId = Thread.currentThread().id
            MinecraftServer.getCommandManager().register(testCommandExecutor)
            MinecraftServer.getCommandManager().executeServerCommand("unittest")
        }

        Thread.sleep(2000)

        Assertions.assertNotEquals(unitTestThreadId, testCommandExecutor.callThreadId)
        Assertions.assertNotEquals(unitTestThreadId, testCommandExecutor.asyncThreadId)
        Assertions.assertNotEquals(unitTestThreadId, testCommandExecutor.leaveThreadId)
        Assertions.assertEquals(testCommandExecutor.callThreadId, testCommandExecutor.leaveThreadId)
        Assertions.assertNotEquals(testCommandExecutor.asyncThreadId, testCommandExecutor.leaveThreadId)
        Assertions.assertNotEquals(testCommandExecutor.callThreadId, testCommandExecutor.asyncThreadId)
    }

    private class TestCommandExecutor(private val extension: Extension) : Command("unittest") {
        var callThreadId = 0L
        var asyncThreadId = 0L
        var leaveThreadId = 0L

        init {
            this.setSuspendingDefaultExecutor(extension) { sender, context ->
                callThreadId = Thread.currentThread().id

                withContext(Dispatchers.IO) {
                    asyncThreadId = Thread.currentThread().id
                    Thread.sleep(50)
                }

                leaveThreadId = Thread.currentThread().id
            }

        }
    }
}
