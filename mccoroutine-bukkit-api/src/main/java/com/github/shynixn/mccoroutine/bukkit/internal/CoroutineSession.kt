package com.github.shynixn.mccoroutine.bukkit.internal

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

interface CoroutineSession {
    /**
     * Gets the plugin scope.
     */
    val scope: CoroutineScope

    /**
     * Gets the event service.
     */
    val eventService: EventService

    /**
     * Gets the command service.
     */
    val commandService: CommandService

    /**
     * Gets the minecraft dispatcher.
     */
    val dispatcherMinecraft: CoroutineContext

    /**
     * Gets the async dispatcher.
     */
    val dispatcherAsync: CoroutineContext

    /**
     * Gets the block service during startup.
     */
    val wakeUpBlockService: WakeUpBlockService

    /**
     * Disposes the session.
     */
    fun dispose()
}
