package com.github.shynixn.mccoroutine.bungeecord.impl

import com.github.shynixn.mccoroutine.bungeecord.CoroutineSession
import com.github.shynixn.mccoroutine.bungeecord.MCCoroutineExceptionEvent
import com.github.shynixn.mccoroutine.bungeecord.dispatcher.BungeeCordCoroutineDispatcher
import com.github.shynixn.mccoroutine.bungeecord.service.BungeeCordEventServiceImpl
import kotlinx.coroutines.*
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext

internal class CoroutineSessionImpl(private val plugin: Plugin) : CoroutineSession {
    private val eventService: BungeeCordEventServiceImpl by lazy {
        BungeeCordEventServiceImpl(plugin)
    }

    /**
     * Plugin scope.
     */
    override val scope: CoroutineScope

    /**
     * BungeeCord Dispatcher.
     */
    override val dispatcherBungeeCord: CoroutineContext by lazy {
        BungeeCordCoroutineDispatcher(plugin)
    }

    init {
        // Root Exception Handler. All Exception which are not consumed by the caller end up here.
        val exceptionHandler = CoroutineExceptionHandler { _, e ->
            val mcCoroutineExceptionEvent = MCCoroutineExceptionEvent(plugin, e)

            plugin.proxy.scheduler.runAsync(plugin, Runnable {
                plugin.proxy.pluginManager.callEvent(mcCoroutineExceptionEvent)

                if (!mcCoroutineExceptionEvent.isCancelled) {
                    if (e !is CancellationException) {
                        plugin.logger.log(
                            Level.SEVERE,
                            "This is not an error of MCCoroutine! See sub exception for details.",
                            e
                        )
                    }
                }
            })
        }

        // Build Coroutine plugin scope for exception handling
        val rootCoroutineScope = CoroutineScope(exceptionHandler)

        // BungeeCord Scope is child of plugin scope and super visor job (e.g. children of a supervisor job can fail independently).
        scope = rootCoroutineScope + SupervisorJob() + dispatcherBungeeCord
    }

    /**
     * Registers a suspend listener.
     */
    override fun registerSuspendListener(listener: Listener) {
        eventService.registerSuspendListener(listener)
    }

    /**
     * Disposes the session.
     */
    fun dispose() {
        scope.coroutineContext.cancelChildren()
    }
}
