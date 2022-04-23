package integrationtest

import com.github.shynixn.mccoroutine.velocity.SuspendingSimpleCommand
import com.github.shynixn.mccoroutine.velocity.executesSuspend
import com.github.shynixn.mccoroutine.velocity.registerSuspend
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import helper.MockedVelocityServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class VelocityCommandTest {
    /**
     *  Given
     *  a call of a simple suspending command
     *  When
     *  dispatchCommand is called from a velocity context
     *  Then
     *  the command should be called on the correct threads.
     */
    @Test
    fun dispatchCommand_SimpleSuspendingCommandExecutor_ShouldCallOnCorrectThreads() {
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

        // Velocity performs a thread switch on executeAsync
        Assertions.assertNotEquals(server.ionNettyDispatcher.threadId, testCommandExecutor.callThreadId)
        Assertions.assertNotEquals(server.ionNettyDispatcher.threadId, testCommandExecutor.asyncThreadId)
        Assertions.assertNotEquals(server.ionNettyDispatcher.threadId, testCommandExecutor.leaveThreadId)
        Assertions.assertNotEquals(testCommandExecutor.asyncThreadId, testCommandExecutor.leaveThreadId)
        Assertions.assertNotEquals(testCommandExecutor.callThreadId, testCommandExecutor.asyncThreadId)
    }

    /**
     *  Given
     *  a call of a brigadier suspending command
     *  When
     *  dispatchCommand is called from a velocity context
     *  Then
     *  the command should be called on the correct threads.
     */
    @Test
    fun dispatchCommand_BrigadierSuspendingCommandExecutor_ShouldCallOnCorrectThreads() {
        val server = MockedVelocityServer()
        val plugin = server.boot()
        var callThreadId = 0L
        var asyncThreadId = 0L
        var leaveThreadId = 0L

        // Commands in Velocity do always arrive on network threads.
        runBlocking(server.ionNettyDispatcher) {
            val builder = LiteralArgumentBuilder
                .literal<CommandSource>("test")
                .executesSuspend(plugin) { context ->
                    callThreadId = Thread.currentThread().id

                    withContext(Dispatchers.IO) {
                        asyncThreadId = Thread.currentThread().id
                        Thread.sleep(50)
                    }

                    leaveThreadId = Thread.currentThread().id
                    1
                }
                .build()
            val command = BrigadierCommand(builder)
            server.proxyServer.commandManager.register(command)
            server.proxyServer.commandManager.executeAsync(Mockito.mock(Player::class.java), "test").get()
        }

        Thread.sleep(250)

        // Velocity performs a thread switch on executeAsync
        Assertions.assertNotEquals(server.ionNettyDispatcher.threadId, callThreadId)
        Assertions.assertNotEquals(server.ionNettyDispatcher.threadId, asyncThreadId)
        Assertions.assertNotEquals(server.ionNettyDispatcher.threadId, leaveThreadId)
        Assertions.assertNotEquals(asyncThreadId, leaveThreadId)
        Assertions.assertNotEquals(callThreadId, asyncThreadId)
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
