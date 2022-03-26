package com.github.shynixn.mccoroutine.contract

import com.github.shynixn.mccoroutine.SuspendingCommand

interface CommandService {
    /**
     * Registers a suspend command executor.
     */
    fun registerSuspendCommandExecutor(command: SuspendingCommand)
}
