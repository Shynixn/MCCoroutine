package com.github.shynixn.mccoroutine.sponge

import org.spongepowered.api.event.Cancellable
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.cause.EventContext
import org.spongepowered.api.event.impl.AbstractEvent
import org.spongepowered.api.plugin.PluginContainer

/**
 * A bukkit event which is called when an exception is raised in one of the coroutines managed by MCCoroutine.
 * Cancelling this exception causes the error to not get logged and offers to possibility for custom logging.
 */
class MCCoroutineExceptionEvent(
    /**
     * Plugin causing the exception.
     */
    val plugin: PluginContainer,
    /**
     * The exception to be logged.
     */
    val exception: Throwable
) : AbstractEvent(), Cancellable {
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

    /**
     * Gets the cause for the event.  The Cause of the event
     * is an object which implements the Iterable interface.
     * So, when investigating the Cause of the event a common
     * idiom is to use operations (functions) on the result
     * of getCause as follows:
     *
     * Use-case: Getting the Player (if any) responsible:
     * `Optional<Player> optPlayer = event.getCause().first(Player.class);`
     *
     * @return The cause
     */
    override fun getCause(): Cause {
        return Cause.builder().append(exception).build(EventContext.empty())
    }
}
