package com.github.shynixn.mccoroutine.bukkit.service

import com.github.shynixn.mccoroutine.bukkit.*
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

internal class CommandServiceImpl(private val plugin: Plugin) {
    /**
     * Registers a suspend command executor.
     */
    fun registerSuspendCommandExecutor(
        context: CoroutineContext,
        pluginCommand: PluginCommand,
        commandExecutor: SuspendingCommandExecutor
    ) {
        pluginCommand.setExecutor { p0, p1, p2, p3 ->
            // If the result is delayed we can automatically assume it is true.
            var success = true

            // Commands in spigot always arrive synchronously. Therefore, we can simply use the default properties.
            plugin.launch(context) {
                success = commandExecutor.onCommand(p0, p1, p2, p3)
            }

            success
        }
    }

    /**
     * Registers a suspend tab completer.
     */
    fun registerSuspendTabCompleter(
        context: CoroutineContext,
        pluginCommand: PluginCommand,
        tabCompleter: SuspendingTabCompleter
    ) {
        pluginCommand.setTabCompleter { sender, command, alias, args ->
            var result : List<String>? = null

            // Tab Completes in spigot always arrive synchronously. Therefore, we can simply use the default properties.
            plugin.launch(context) {
                result = tabCompleter.onTabComplete(sender, command, alias, args)
            }

            result
        }
    }
}
