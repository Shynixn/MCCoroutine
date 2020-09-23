package com.github.shynixn.mccoroutine.contract

import com.github.shynixn.mccoroutine.CommandEvent
import com.github.shynixn.mccoroutine.SuspendingCommandExecutor
import kotlinx.coroutines.flow.Flow
import org.bukkit.command.PluginCommand

interface CommandService {
    /**
     * Registers a suspend command executor.
     */
    fun registerSuspendCommandExecutor(pluginCommand: PluginCommand, commandExecutor: SuspendingCommandExecutor)
}
