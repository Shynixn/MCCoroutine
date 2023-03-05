package com.github.shynixn.mccoroutine.fabric

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Facade of a coroutine session of a single extension or entire server.
 */
interface CoroutineSession {
    /**
     * Lifetime scope.
     */
    val scope: CoroutineScope

    /**
     * Minecraft Dispatcher.
     */
    val dispatcherMinecraft: CoroutineContext

    /**
     * MCCoroutine Facade.
     */
    val mcCoroutineConfiguration: MCCoroutineConfiguration
}
