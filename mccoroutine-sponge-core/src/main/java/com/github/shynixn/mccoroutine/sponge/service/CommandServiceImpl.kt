package com.github.shynixn.mccoroutine.sponge.service

import com.github.shynixn.mccoroutine.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.contract.CommandService
import com.github.shynixn.mccoroutine.contract.CoroutineSession
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.plugin.PluginContainer

internal class CommandServiceImpl(private val plugin: PluginContainer, private val coroutineSession: CoroutineSession) :
    CommandService {
    /**
     * Registers a suspend command executor.
     */
    override fun registerSuspendCommandExecutor(
        commandSpec: CommandSpec.Builder,
        commandExecutor: SuspendingCommandExecutor
    ) {
        commandSpec.executor { src, args ->
            var commandResult = CommandResult.success();

            coroutineSession.launch(coroutineSession.dispatcherMinecraft) {
                commandResult = commandExecutor.execute(src, args)
            }

            commandResult
        }
    }
}
