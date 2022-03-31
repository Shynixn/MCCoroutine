package integrationtest

import com.github.shynixn.mccoroutine.bungeecord.SuspendingCommand
import com.github.shynixn.mccoroutine.bungeecord.registerSuspendingCommand
import helper.MockedBungeeCordServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class BungeeCordCommandTest {
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
        val server = MockedBungeeCordServer()
        val plugin = server.boot()
        val testCommandExecutor = TestCommandExecutor(plugin)

        // Events in BungeeCord do always arrive on network threads.
        runBlocking(server.ionNettyDispatcher) {
            plugin.proxy.pluginManager.registerSuspendingCommand(plugin, testCommandExecutor)
            plugin.proxy.pluginManager.dispatchCommand(Mockito.mock(ProxiedPlayer::class.java), "test")
        }

        Thread.sleep(250)

        Assertions.assertEquals(server.ionNettyDispatcher.threadId, testCommandExecutor.callThreadId)
        Assertions.assertNotEquals(server.ionNettyDispatcher.threadId, testCommandExecutor.asyncThreadId)
        Assertions.assertNotEquals(server.ionNettyDispatcher.threadId, testCommandExecutor.leaveThreadId)
        Assertions.assertNotEquals(testCommandExecutor.asyncThreadId, testCommandExecutor.leaveThreadId)
    }

    private class TestCommandExecutor(private val plugin: Plugin) : SuspendingCommand("test") {
        var callThreadId = 0L
        var asyncThreadId = 0L
        var leaveThreadId = 0L

        override suspend fun execute(sender: CommandSender, args: Array<out String>) {
            callThreadId = Thread.currentThread().id

            withContext(Dispatchers.IO) {
                asyncThreadId = Thread.currentThread().id
                Thread.sleep(50)
            }

            leaveThreadId = Thread.currentThread().id
        }
    }
}
