package com.github.shynixn.mccoroutine.fabric

import com.mojang.brigadier.context.CommandContext

/**
 * A suspending interface of the default command.
 */
@FunctionalInterface
interface SuspendingCommand<S> {
    /**
     * Gets called when the command is executed.
     */
    suspend fun run(context: CommandContext<S>): Int
}
