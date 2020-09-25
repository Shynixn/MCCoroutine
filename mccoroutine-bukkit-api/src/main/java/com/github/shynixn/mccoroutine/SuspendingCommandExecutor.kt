package com.github.shynixn.mccoroutine

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

interface SuspendingCommandExecutor {
    /**
     * Executes the given command, returning its success.
     * If false is returned, then the "usage" plugin.yml entry for this command (if defined) will be sent to the player.
     */
    suspend fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean
}
