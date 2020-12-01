package com.github.shynixn.mccoroutine.contract

import com.github.shynixn.mccoroutine.SuspendingCommandExecutor
import org.spongepowered.api.command.spec.CommandSpec

interface CommandService {
    /**
     * Registers a suspend command executor.
     */
    fun registerSuspendCommandExecutor(commandSpec: CommandSpec.Builder, commandExecutor: SuspendingCommandExecutor)
}
