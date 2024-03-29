package com.github.shynixn.mccoroutine.folia

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

/**
 * Represents a class which contains a single method for executing commands
 */
interface SuspendingCommandExecutor {
    /**
     * Executes the given command, returning its success.
     * If false is returned, then the "usage" plugin.yml entry for this command (if defined) will be sent to the player.
     * @param sender - Source of the command.
     * @param command - Command which was executed.
     * @param label - Alias of the command which was used.
     * @param args - Passed command arguments.
     * @return True if a valid command, otherwise false.
     */
    suspend fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean
}
