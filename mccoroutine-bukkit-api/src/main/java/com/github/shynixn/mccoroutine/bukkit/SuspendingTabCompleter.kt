package com.github.shynixn.mccoroutine.bukkit

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

/**
 * Represents a suspending class which can suggest tab completions for commands.
 */
interface SuspendingTabCompleter {
    /**
     * Requests a list of possible completions for a command argument.
     * If the call is suspended during the execution, the returned list will not be shown.
     * @param sender - Source of the command.
     * @param command - Command which was executed.
     * @param alias - Alias of the command which was used.
     * @param args - Passed command arguments.
     * @return A list of possible completions for the final argument, or an empty list.
     */
    suspend fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>
}
