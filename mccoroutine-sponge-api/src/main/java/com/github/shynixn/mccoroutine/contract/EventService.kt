package com.github.shynixn.mccoroutine.contract

interface EventService {
    /**
     * Registers a suspend listener.
     */
    fun registerSuspendListener(listener: Any)
}
