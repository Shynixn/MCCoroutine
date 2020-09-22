package com.github.shynixn.mccoroutine.entity

import com.github.shynixn.mccoroutine.contract.CoroutineSession
import com.github.shynixn.mccoroutine.contract.EventService
import com.github.shynixn.mccoroutine.dispatcher.MinecraftCoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.ProducerScope
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

internal class CoroutineSessionImpl(private val plugin: Plugin) : CoroutineSession {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var disposed = false

    override val flows: MutableMap<Listener, ProducerScope<Event>> = HashMap()

    /**
     * Gets the event service.
     */
    override val eventService: EventService by lazy {
        EventServiceImpl(plugin, this)
    }

    /**
     * Gets the minecraft dispatcher.
     */
    override val dispatcherMinecraft: CoroutineContext by lazy {
        MinecraftCoroutineDispatcher(plugin)
    }

    /**
     * Disposes the session.
     */
    override fun dispose() {
        disposed = true
        flows.values.forEach { e -> e.channel.close() }
        flows.clear()
        scope.coroutineContext.cancelChildren()
    }

    /**
     * Launches the given function on the Minecraft Thread and handles
     * coroutine scopes correctly.
     */
    override fun launchOnMinecraft(f: suspend CoroutineScope.() -> Unit) {
        TODO("Not yet implemented")
    }

    /**
     * Launches the given function on an Async Thread and handles
     * coroutine scopes correctly.
     */
    override fun launchOnAsync(f: suspend CoroutineScope.() -> Unit) {
        TODO("Not yet implemented")
    }
}
