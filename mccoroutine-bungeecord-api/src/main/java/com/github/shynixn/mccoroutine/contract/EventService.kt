package com.github.shynixn.mccoroutine.contract

import net.md_5.bungee.api.plugin.Listener

interface EventService {
    /**
     * Registers a suspend listener.
     */
    fun registerSuspendListener(listener: Listener)
}
