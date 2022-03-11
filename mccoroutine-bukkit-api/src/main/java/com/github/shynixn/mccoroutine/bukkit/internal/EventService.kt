package com.github.shynixn.mccoroutine.bukkit.internal

import kotlinx.coroutines.Job
import org.bukkit.event.Event
import org.bukkit.event.Listener

interface EventService {
    /**
     * Registers a suspend listener.
     */
    fun registerSuspendListener(listener: Listener)

    /**
     * Fires a suspending [event] with the given [eventExecutionType].
     * @return Collection of receiver jobs. May already be completed.
     */
    fun fireSuspendingEvent(event: Event, eventExecutionType: com.github.shynixn.mccoroutine.bukkit.EventExecutionType): Collection<Job>
}
