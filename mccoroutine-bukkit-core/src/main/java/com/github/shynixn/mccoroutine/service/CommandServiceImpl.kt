package com.github.shynixn.mccoroutine.service

import com.github.shynixn.mccoroutine.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.contract.CommandService
import com.github.shynixn.mccoroutine.contract.CoroutineSession
import com.github.shynixn.mccoroutine.minecraftDispatcher
import kotlinx.coroutines.Dispatchers
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.Plugin

internal class CommandServiceImpl(private val plugin: Plugin, private val coroutineSession: CoroutineSession) :
    CommandService {
    /**
     * Registers a suspend command executor.
     */
    override fun registerSuspendCommandExecutor(
        pluginCommand: PluginCommand,
        commandExecutor: SuspendingCommandExecutor
    ) {
        pluginCommand.setExecutor { p0, p1, p2, p3 ->
            // If the result is delayed we can automatically assume it is true.
            var success = true

            coroutineSession.launch(plugin.minecraftDispatcher) {
                success = commandExecutor.onCommand(p0, p1, p2, p3)
            }

            success
        }
    }
}
