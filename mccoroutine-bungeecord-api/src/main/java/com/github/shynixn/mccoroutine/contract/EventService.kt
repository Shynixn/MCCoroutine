package com.github.shynixn.mccoroutine.contract

import kotlinx.coroutines.Job
import net.md_5.bungee.api.plugin.Event
import net.md_5.bungee.api.plugin.Listener

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
