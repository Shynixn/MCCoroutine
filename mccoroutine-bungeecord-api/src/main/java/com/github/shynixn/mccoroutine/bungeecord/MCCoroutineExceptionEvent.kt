package com.github.shynixn.mccoroutine.bungeecord

import net.md_5.bungee.api.plugin.Cancellable
import net.md_5.bungee.api.plugin.Event
import net.md_5.bungee.api.plugin.Plugin

/**
 * A BungeeCord event which is called when an exception is raised in one of the coroutines managed by MCCoroutine.
 * Cancelling this exception causes the error to not get logged and offers to possibility for custom logging.
 */
class MCCoroutineExceptionEvent(
    /**
     * Plugin causing the exception.
     */
    val plugin: Plugin,
    /**
     * The exception to be logged.
     */
    val exception: Throwable
) : Event(), Cancellable {
    private var cancelled: Boolean = false

    /**
     * Gets if this event is cancelled.
     */
    override fun isCancelled(): Boolean {
        return cancelled
    }

    /**
     * Sets the event as cancelled or not. If the event is cancelled
     * the exception is seen as an uncaught exception. Do only cancel this event
     * if you want to log the exceptions on your own.
     */
    override fun setCancelled(flag: Boolean) {
        this.cancelled = flag
    }
}
