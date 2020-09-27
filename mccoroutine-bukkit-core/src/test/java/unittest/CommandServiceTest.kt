package unittest

import com.github.shynixn.mccoroutine.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.service.CommandServiceImpl
import helper.MockedCoroutineSession
import helper.any
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import kotlin.test.assertTrue

class CommandServiceTest {
    /**
     * Given a suspending command executor
     * When registerSuspendCommandExecutor is called
     * then the suspending command executor should be callable.
     */
    @Test
    fun registerSuspendCommandExecutor_ValidParameters_ShouldRegisterExecutor() {
        // Arrange
        val plugin = Mockito.mock(Plugin::class.java)
        var commandExecutorCalled = false
        val suspendingCommandExecutor = object : SuspendingCommandExecutor {
            override suspend fun onCommand(
                sender: CommandSender,
                command: Command,
                label: String,
                args: Array<out String>
            ): Boolean {
                commandExecutorCalled = true
                return true
            }
        }
        val pluginCommand = Mockito.mock(PluginCommand::class.java)
        var wrappedCommandExecutor: CommandExecutor? = null
        Mockito.`when`(pluginCommand.setExecutor(any(CommandExecutor::class.java))).thenAnswer {
            wrappedCommandExecutor = it.arguments[0] as CommandExecutor
            Unit
        }
        val classUnderTest = createWithDependencies(plugin)

        // Act
        classUnderTest.registerSuspendCommandExecutor(pluginCommand, suspendingCommandExecutor)
        wrappedCommandExecutor!!.onCommand(
            Mockito.mock(CommandSender::class.java),
            Mockito.mock(Command::class.java),
            "",
            emptyArray()
        )

        // Assert
        assertTrue(commandExecutorCalled)
    }

    private fun createWithDependencies(plugin: Plugin): CommandServiceImpl {
        return CommandServiceImpl(plugin, MockedCoroutineSession())
    }
}
