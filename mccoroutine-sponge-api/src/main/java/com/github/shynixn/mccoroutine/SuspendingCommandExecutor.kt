package com.github.shynixn.mccoroutine

import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext

/**
 * Interface containing the method directing how a certain command will
 * be executed.
 */
@FunctionalInterface
interface SuspendingCommandExecutor {
    /**
     * Callback for the execution of a command.
     *
     * @param src The commander who is executing this command
     * @param args The parsed command arguments for this command
     * @return the result of executing this command.
     */
    suspend fun execute(src: CommandSource, args: CommandContext): CommandResult
}

