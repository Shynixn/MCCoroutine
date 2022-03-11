package com.github.shynixn.mccoroutine.bukkit.service

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.bukkit.SuspendingTabCompleter
import com.github.shynixn.mccoroutine.bukkit.internal.CommandService
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import kotlinx.coroutines.CoroutineStart
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

            plugin.launch(plugin.minecraftDispatcher, CoroutineStart.UNDISPATCHED) {
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

            plugin.launch(plugin.minecraftDispatcher, CoroutineStart.UNDISPATCHED) {
                result = tabCompleter.onTabComplete(sender, command, alias, args)
            }

            result
        }
    }
}
