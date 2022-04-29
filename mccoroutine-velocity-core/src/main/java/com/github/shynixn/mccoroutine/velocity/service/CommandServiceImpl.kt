package com.github.shynixn.mccoroutine.velocity.service

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.github.shynixn.mccoroutine.velocity.SuspendingSimpleCommand
import com.github.shynixn.mccoroutine.velocity.launch
import com.github.shynixn.mccoroutine.velocity.velocityDispatcher
import com.velocitypowered.api.command.CommandMeta
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.plugin.PluginContainer
import kotlinx.coroutines.CoroutineStart
import java.util.concurrent.CompletableFuture

internal class CommandServiceImpl(
    private val pluginContainer: PluginContainer,
    private val suspendingPluginContainer: SuspendingPluginContainer
) {
    /**
     * Registers a command to velocity.
     */
    fun registerCommand(meta: CommandMeta, command: SuspendingSimpleCommand) {
        val commandManager = suspendingPluginContainer.server.commandManager
        val wrapper = InternalSimpleCommand(pluginContainer, command)
        commandManager.register(meta, wrapper)
    }

    private class InternalSimpleCommand(
        private val plugin: PluginContainer,
        private val handle: SuspendingSimpleCommand
    ) : SimpleCommand {
        /**
         * Executes the command for the specified invocation.
         *
         * @param invocation the invocation context
         */
        override fun execute(invocation: SimpleCommand.Invocation) {
            // Start unDispatched on the same thread but end up on the velocity dispatcher.
            plugin.launch(plugin.velocityDispatcher, CoroutineStart.UNDISPATCHED) {
                handle.execute(invocation)
            }
        }

        /**
         * Provides tab complete suggestions for the specified invocation.
         *
         * @param invocation the invocation context
         * @return the tab complete suggestions
         * @implSpec defaults to wrapping the value returned by [.suggest]
         */
        override fun suggestAsync(invocation: SimpleCommand.Invocation): CompletableFuture<MutableList<String>> {
            val completableFuture = CompletableFuture<MutableList<String>>()

            // Start unDispatched on the same thread but end up on the velocity dispatcher.
            plugin.launch(plugin.velocityDispatcher, CoroutineStart.UNDISPATCHED) {
                val result = handle.suggest(invocation)
                completableFuture.complete(result.toMutableList())
            }

            return completableFuture;
        }

        /**
         * Tests to check if the source has permission to perform the specified invocation.
         *
         *
         * If the method returns `false`, the handling is forwarded onto
         * the players current server.
         *
         * @param invocation the invocation context
         * @return `true` if the source has permission
         */
        override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
            return handle.hasPermission(invocation)
        }
    }
}
