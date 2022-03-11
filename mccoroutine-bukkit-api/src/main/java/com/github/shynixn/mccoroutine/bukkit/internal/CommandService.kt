package com.github.shynixn.mccoroutine.bukkit.internal

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.bukkit.SuspendingTabCompleter
import org.bukkit.command.PluginCommand

interface CommandService {
    /**
     * Registers a suspend command executor.
     */
    fun registerSuspendCommandExecutor(pluginCommand: PluginCommand, commandExecutor: SuspendingCommandExecutor)

    /**
     * Registers a suspend tab completer.
     */
    fun registerSuspendTabCompleter(pluginCommand: PluginCommand, tabCompleter: SuspendingTabCompleter)
}
