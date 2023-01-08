package com.github.shynixn.mccoroutine.minestom

import net.minestom.server.event.Event
import net.minestom.server.event.trait.CancellableEvent
import net.minestom.server.extensions.Extension

/**
 * A Minestom event which is called when an exception is raised in one of the coroutines managed by MCCoroutine.
 * Cancelling this exception causes the error to not get logged and offers to possibility for custom logging.
 */
class MCCoroutineExceptionEvent(
    /**
     * Extension causing the exception.
     * Is null if the exception is thrown from the root Minecraft Server implementation.
     */
    val plugin: Extension?,
    /**
     * The exception to be logged.
     */
    val exception: Throwable
) : CancellableEvent {
    private var cancelled: Boolean = false

    /**
     * Gets if the [Event] should be cancelled or not.
     *
     * @return true if the event should be cancelled
     */
    override fun isCancelled(): Boolean {
        return cancelled
    }

    /**
     * Marks the [Event] as cancelled or not.
     *
     * @param cancel true if the event should be cancelled, false otherwise
     */
    override fun setCancelled(cancel: Boolean) {
        this.cancelled = cancel
    }
}
