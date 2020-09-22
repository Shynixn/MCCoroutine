package com.github.shynixn.mccoroutine.contract

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import org.bukkit.event.Event
import org.bukkit.event.Listener
import kotlin.coroutines.CoroutineContext

interface CoroutineSession {
    /**
     * Session flows.
     */
    val flows: MutableMap<Listener, ProducerScope<Event>>

    /**
     * Gets the event service.
     */
    val eventService: EventService

    /**
     * Gets the minecraft dispatcher.
     */
    val dispatcherMinecraft: CoroutineContext

    /**
     * Launches the given function on the Minecraft Thread and handles
     * coroutine scopes correctly.
     */
    fun launchOnMinecraft(f: suspend CoroutineScope.() -> Unit)

    /**
     * Launches the given function on an Async Thread and handles
     * coroutine scopes correctly.
     */
    fun launchOnAsync(f: suspend CoroutineScope.() -> Unit)

    /**
     * Disposes the session.
     */
    fun dispose()
}
