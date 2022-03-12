package integrationtest

import com.github.shynixn.mccoroutine.bukkit.*
import helper.MockedBukkitServer
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class BukkitCommandTest {
    /**
     *  Given
     *  a call of a suspending command
     *  When
     *  dispatchCommand is called from a minecraft context
     *  Then
     *  the command should be called on the correct threads.
     */
    @Test
    fun dispatchCommand_SuspendingCommandExecutor_ShouldCallOnCorrectThreads() {
        val server = MockedBukkitServer()
        val plugin = server.boot()
        val testCommandExecutor = TestCommandExecutor(plugin)
        val pluginCommand = plugin.server.getPluginCommand("test")!!

        runBlocking(plugin.minecraftDispatcher) {
            pluginCommand.setSuspendingExecutor(testCommandExecutor)
            plugin.server.dispatchCommand(Mockito.mock(Player::class.java), "test")
        }
        Thread.sleep(250)

        Assertions.assertEquals(server.mainThreadId, testCommandExecutor.callThreadId)
        Assertions.assertNotEquals(server.mainThreadId, testCommandExecutor.asyncThreadId)
        Assertions.assertEquals(server.mainThreadId, testCommandExecutor.leaveThreadId)
    }

    /**
     *  Given
     *  a call of a suspending tab completer
     *  When
     *  tabComplete is called from a minecraft context
     *  Then
     *  the tab completer should be called on the correct threads.
     */
    @Test
    fun dispatchCommand_SuspendingTabCompleter_ShouldCallOnCorrectThreads() {
        val server = MockedBukkitServer()
        val plugin = server.boot()
        val testCommandExecutor = TestCommandExecutor(plugin)
        val pluginCommand = plugin.server.getPluginCommand("test")!!

        runBlocking(plugin.minecraftDispatcher) {
            pluginCommand.setSuspendingTabCompleter(testCommandExecutor)
            server.commandMap.tabComplete(Mockito.mock(Player::class.java), "test me")
        }
        Thread.sleep(250)

        Assertions.assertEquals(server.mainThreadId, testCommandExecutor.callThreadId)
        Assertions.assertNotEquals(server.mainThreadId, testCommandExecutor.asyncThreadId)
        Assertions.assertEquals(server.mainThreadId, testCommandExecutor.leaveThreadId)
    }

    private class TestCommandExecutor(private val plugin: Plugin) : SuspendingCommandExecutor, SuspendingTabCompleter {
        var callThreadId = 0L
        var asyncThreadId = 0L
        var leaveThreadId = 0L

        /**
         * Executes the given command, returning its success.
         * If false is returned, then the "usage" plugin.yml entry for this command (if defined) will be sent to the player.
         * @param sender - Source of the command.
         * @param command - Command which was executed.
         * @param label - Alias of the command which was used.
         * @param args - Passed command arguments.
         * @return True if a valid command, otherwise false.
         */
        override suspend fun onCommand(
            sender: CommandSender,
            command: Command,
            label: String,
            args: Array<out String>
        ): Boolean {
            callThreadId = Thread.currentThread().id

            withContext(plugin.asyncDispatcher) {
                asyncThreadId = Thread.currentThread().id
                Thread.sleep(50)
            }

            leaveThreadId = Thread.currentThread().id
            return true
        }

        /**
         * Requests a list of possible completions for a command argument.
         * If the call is suspended during the execution, the returned list will not be shown.
         * @param sender - Source of the command.
         * @param command - Command which was executed.
         * @param alias - Alias of the command which was used.
         * @param args - Passed command arguments.
         * @return A list of possible completions for the final argument, or an empty list.
         */
        override suspend fun onTabComplete(
            sender: CommandSender,
            command: Command,
            alias: String,
            args: Array<out String>
        ): List<String> {
            callThreadId = Thread.currentThread().id

            withContext(plugin.asyncDispatcher) {
                asyncThreadId = Thread.currentThread().id
                Thread.sleep(50)
            }

            leaveThreadId = Thread.currentThread().id
            return arrayListOf()
        }
    }
}
