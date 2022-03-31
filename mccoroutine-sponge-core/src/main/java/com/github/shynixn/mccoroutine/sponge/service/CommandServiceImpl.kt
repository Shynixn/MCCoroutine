package com.github.shynixn.mccoroutine.sponge.service

import com.github.shynixn.mccoroutine.sponge.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.sponge.launch
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.plugin.PluginContainer

internal class CommandServiceImpl(private val plugin: PluginContainer) {
    /**
     * Registers a suspend command executor.
     */
    fun registerSuspendCommandExecutor(
        commandSpec: CommandSpec.Builder,
        commandExecutor: SuspendingCommandExecutor
    ) {
        commandSpec.executor { src, args ->
            var commandResult = CommandResult.success();

            // Commands in sponge always arrive synchronously. Therefore, we can simply use the default properties.
            plugin.launch {
                commandResult = commandExecutor.execute(src, args)
            }

            commandResult
        }
    }
}
