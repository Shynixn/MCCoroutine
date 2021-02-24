package com.github.shynixn.mccoroutine.contract

import kotlinx.coroutines.Job
import org.spongepowered.api.event.Event

interface EventService {
    /**
     * Registers a suspend listener.
     */
    fun registerSuspendListener(listener: Any)

    /**
     * Fires a suspending event.
     */
    fun fireSuspendingEvent(event: Event): Collection<Job>
}
