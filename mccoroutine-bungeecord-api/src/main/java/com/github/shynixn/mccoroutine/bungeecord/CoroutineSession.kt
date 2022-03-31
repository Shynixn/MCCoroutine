package com.github.shynixn.mccoroutine.bungeecord

import kotlinx.coroutines.CoroutineScope
import net.md_5.bungee.api.plugin.Listener
import kotlin.coroutines.CoroutineContext

/**
 * Facade of a coroutine session of a single plugin.
 */
interface CoroutineSession {
    /**
     * Plugin scope.
     */
    val scope: CoroutineScope

    /**
     * BungeeCord Dispatcher.
     */
    val dispatcherBungeeCord: CoroutineContext

    /**
     * Registers a suspend listener.
     */
    fun registerSuspendListener(listener: Listener)
}
