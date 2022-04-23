package integrationtest

import com.github.shynixn.mccoroutine.velocity.SuspendingSimpleCommand
import com.github.shynixn.mccoroutine.velocity.registerSuspend
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import helper.MockedVelocityServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class VelocityCommandTest {
    /**
     *  Given
     *  a call of a suspending command
     *  When
     *  dispatchCommand is called from a bungeeCord context
     *  Then
     *  the command should be called on the correct threads.
     */
    @Test
    fun dispatchCommand_SuspendingCommandExecutor_ShouldCallOnCorrectThreads() {
        val server = MockedVelocityServer()
        val plugin = server.boot()
        val testCommandExecutor = TestCommandExecutor()

        // Commands in Velocity do always arrive on network threads.
        runBlocking(server.ionNettyDispatcher) {
            val meta = server.proxyServer.commandManager.metaBuilder("test").build()
            server.proxyServer.commandManager.registerSuspend(meta, testCommandExecutor, plugin)
            server.proxyServer.commandManager.executeAsync(Mockito.mock(Player::class.java), "test").get()
        }

        Thread.sleep(250)

        Assertions.assertNotEquals(server.ionNettyDispatcher.threadId, testCommandExecutor.callThreadId)
        Assertions.assertNotEquals(server.ionNettyDispatcher.threadId, testCommandExecutor.asyncThreadId)
        Assertions.assertNotEquals(server.ionNettyDispatcher.threadId, testCommandExecutor.leaveThreadId)
        Assertions.assertNotEquals(testCommandExecutor.asyncThreadId, testCommandExecutor.leaveThreadId)
        Assertions.assertNotEquals(testCommandExecutor.callThreadId, testCommandExecutor.asyncThreadId)
    }

    private class TestCommandExecutor : SuspendingSimpleCommand {
        var callThreadId = 0L
        var asyncThreadId = 0L
        var leaveThreadId = 0L

        /**
         * Executes the command for the specified invocation.
         *
         * @param invocation the invocation context
         */
        override suspend fun execute(invocation: SimpleCommand.Invocation) {
            callThreadId = Thread.currentThread().id

            withContext(Dispatchers.IO) {
                asyncThreadId = Thread.currentThread().id
                Thread.sleep(50)
            }

            leaveThreadId = Thread.currentThread().id
        }
    }
}
