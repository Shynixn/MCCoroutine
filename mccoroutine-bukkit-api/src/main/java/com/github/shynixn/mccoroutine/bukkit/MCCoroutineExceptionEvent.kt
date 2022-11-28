package com.github.shynixn.mccoroutine.bukkit

import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.server.ServerEvent
import org.bukkit.plugin.Plugin

/**
 * A Bukkit event which is called when an exception is raised in one of the coroutines managed by MCCoroutine.
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
) : ServerEvent(false), Cancellable {
    private var cancelled: Boolean = false

    /**
     * Event.
     */
    companion object {
        private var handlers = HandlerList()

        /**
         * Handlerlist.
         */
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlers
        }
    }

    /**
     * Returns all handles.
     */
    override fun getHandlers(): HandlerList {
        return MCCoroutineExceptionEvent.handlers
    }

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
