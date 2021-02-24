package com.github.shynixn.mccoroutine.contract

import kotlinx.coroutines.Job
import org.bukkit.event.Event
import org.bukkit.event.Listener

interface EventService {
    /**
     * Registers a suspend listener.
     */
    fun registerSuspendListener(listener: Listener)

    /**
     * Fires a suspending event.
     */
    fun fireSuspendingEvent(event: Event): Collection<Job>
}
