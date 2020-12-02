package com.github.shynixn.mccoroutine.service

import com.github.shynixn.mccoroutine.contract.CoroutineSession
import com.github.shynixn.mccoroutine.contract.EventService
import org.spongepowered.api.plugin.PluginContainer

internal class EventServiceImpl(private val plugin: PluginContainer, private val coroutineSession: CoroutineSession) :
    EventService {
    /**
     * Registers a suspend listener.
     */
    override fun registerSuspendListener(listener: Any) {
    }
}
