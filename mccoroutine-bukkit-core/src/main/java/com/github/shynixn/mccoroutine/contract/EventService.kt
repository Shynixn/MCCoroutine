package com.github.shynixn.mccoroutine.contract

import kotlinx.coroutines.flow.Flow
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

interface EventService {
    /**
     * Registers a suspend listener.
     */
    fun registerSuspendListener(listener: Listener, plugin: Plugin)

    /**
     * Creates a new event flow for the given event clazz.
     */
    fun <T : Event> createEventFlow(event: Class<T>): Flow<T>
}
