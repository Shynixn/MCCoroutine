package com.github.shynixn.mccoroutine.bukkit.service

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.bukkit.SuspendingTabCompleter
import com.github.shynixn.mccoroutine.bukkit.internal.CommandService
import com.github.shynixn.mccoroutine.bukkit.launch
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.Plugin

internal class CommandServiceImpl(private val plugin: Plugin) :
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

            // Commands in spigot always arrive synchronously. Therefore, we can simply use the default properties.
            plugin.launch {
                success = commandExecutor.onCommand(p0, p1, p2, p3)
            }

            success
        }
    }

    /**
     * Registers a suspend tab completer.
     */
    override fun registerSuspendTabCompleter(pluginCommand: PluginCommand, tabCompleter: SuspendingTabCompleter) {
        pluginCommand.setTabCompleter { sender, command, alias, args ->
            var result = emptyList<String>()

            // Tab Completes in spigot always arrive synchronously. Therefore, we can simply use the default properties.
            plugin.launch {
                result = tabCompleter.onTabComplete(sender, command, alias, args)
            }

            result
        }
    }
}
