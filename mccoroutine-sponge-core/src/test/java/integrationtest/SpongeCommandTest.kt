package integrationtest

import com.github.shynixn.mccoroutine.sponge.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.sponge.asyncDispatcher
import com.github.shynixn.mccoroutine.sponge.minecraftDispatcher
import com.github.shynixn.mccoroutine.sponge.suspendingExecutor
import helper.MockedSpongeServer
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.plugin.PluginContainer

class SpongeCommandTest {
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
        val server = MockedSpongeServer()
        val plugin = server.boot()
        val testCommandExecutor = TestCommandExecutor(plugin)

        runBlocking(plugin.minecraftDispatcher) {
            val commandSpec = CommandSpec.builder()
                .suspendingExecutor(plugin, testCommandExecutor)
            Sponge.getCommandManager().register(plugin, commandSpec.build(), listOf("test"))
            server.callCommand(Mockito.mock(Player::class.java), "test")
        }
        Thread.sleep(250)

        Assertions.assertEquals(server.mainThreadId, testCommandExecutor.callThreadId)
        Assertions.assertNotEquals(server.mainThreadId, testCommandExecutor.asyncThreadId)
        Assertions.assertEquals(server.mainThreadId, testCommandExecutor.leaveThreadId)
    }

    private class TestCommandExecutor(private val plugin: PluginContainer) : SuspendingCommandExecutor {
        var callThreadId = 0L
        var asyncThreadId = 0L
        var leaveThreadId = 0L

        /**
         * Callback for the execution of a command.
         *
         * @param src The commander who is executing this command
         * @param args The parsed command arguments for this command
         * @return the result of executing this command.
         */
        override suspend fun execute(src: CommandSource, args: CommandContext): CommandResult {
            callThreadId = Thread.currentThread().id

            withContext(plugin.asyncDispatcher) {
                asyncThreadId = Thread.currentThread().id
                Thread.sleep(50)
            }

            leaveThreadId = Thread.currentThread().id
            return CommandResult.success()
        }
    }
}
