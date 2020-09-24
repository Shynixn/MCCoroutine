package com.github.shynixn.mccoroutine.contract

import org.bukkit.event.Listener

interface EventService {
    /**
     * Registers a suspend listener.
     */
    fun registerSuspendListener(listener: Listener)
}
