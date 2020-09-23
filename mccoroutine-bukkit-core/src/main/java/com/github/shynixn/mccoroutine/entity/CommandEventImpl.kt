package com.github.shynixn.mccoroutine.entity

import com.github.shynixn.mccoroutine.CommandEvent
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class CommandEventImpl(
    override val commandSender: CommandSender,
    override val command: Command,
    override val args: Array<String>,
    override val label: String,
    override var success: Boolean = false
) : CommandEvent
