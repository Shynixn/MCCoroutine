package com.github.shynixn.mccoroutine.contract

import kotlinx.coroutines.flow.Flow
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

interface EventService {
    /**
     * Registers a suspend listener.
     */
    fun registerSuspendListener(listener: Listener)
}
