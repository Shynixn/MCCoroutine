package com.github.shynixn.mccoroutine

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command

abstract class SuspendingCommand {
    private val command: WrappedCommand
    val name: String
        get() = command.name
    val permission: String?
        get() = command.permission
    val aliases: Array<String>
        get() = command.aliases
    var permissionMessage: String?
        get() = command.permissionMessage
        protected set(value) {
            command.permissionMessage = value
        }

    constructor(name: String) {
        command = WrappedCommand(name)
    }

    constructor(name: String, permission: String, vararg aliases: String) {
        command = WrappedCommand(name, permission, *aliases)
    }

    abstract suspend fun execute(sender: CommandSender, args: Array<out String>)

    fun hasPermission(sender: CommandSender): Boolean {
        return command.hasPermission(sender)
    }

    /**
     * Indicates whether some other object is "equal to" this one. Implementations must fulfil the following
     * requirements:
     */
    override fun equals(other: Any?): Boolean {
        if (other is SuspendingCommand) {
            return command == other.command
        }

        return false
    }

    /**
     * Returns a hash code value for the object.  The general contract of `hashCode` is:
     *
     * * Whenever it is invoked on the same object more than once, the `hashCode` method must consistently return the same integer, provided no information used in `equals` comparisons on the object is modified.
     * * If two objects are equal according to the `equals()` method, then calling the `hashCode` method on each of the two objects must produce the same integer result.
     */
    override fun hashCode(): Int {
        return command.hashCode()
    }

    /**
     * Returns a string representation of the object.
     */
    override fun toString(): String {
        return command.toString()
    }

    private class WrappedCommand : Command {
        constructor(name: String) : super(name) {
        }

        constructor(name: String, permission: String, vararg aliases: String) : super(name, permission, *aliases) {
        }

        override fun execute(sender: CommandSender?, args: Array<out String>?) {
            // Disabled
        }

        public override fun setPermissionMessage(permissionMessage: String?) {
            // Overwrite because of accessibility modifiers.
            super.setPermissionMessage(permissionMessage)
        }
    }
}
