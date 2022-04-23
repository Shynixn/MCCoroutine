package com.github.shynixn.mccoroutine.velocity

import com.velocitypowered.api.command.Command
import com.velocitypowered.api.command.CommandMeta
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Facade of a coroutine session of a single plugin.
 */
interface CoroutineSession {
    /**
     * Plugin scope.
     */
    val scope: CoroutineScope

    /**
     * Velocity Dispatcher.
     */
    val dispatcherVelocity: CoroutineContext

    /**
     * Registers a suspend listener.
     */
    fun registerSuspendListener(listener: Any)

    /**
     * Registers a suspend command.
     */
    fun registerSuspendCommand(meta: CommandMeta? = null, command: Command)
}
