package com.github.shynixn.mccoroutine

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

interface CommandEvent {
    /**
     * Command sender.
     */
    val commandSender: CommandSender

    /**
     * Command.
     */
    val command: Command

    /**
     * Arguments.
     */
    val args: Array<String>

    /**
     * Label.
     */
    val label: String

    /**
     * Success state of the command.
     * Default false.
     */
    var success: Boolean
}
