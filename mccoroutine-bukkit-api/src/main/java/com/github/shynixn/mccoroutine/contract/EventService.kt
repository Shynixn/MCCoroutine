package com.github.shynixn.mccoroutine.contract

import com.github.shynixn.mccoroutine.EventExecutionType
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
    fun fireSuspendingEvent(event: Event, eventExecutionType: EventExecutionType): Collection<Job>
}
