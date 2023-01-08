package com.github.shynixn.mccoroutine.minestom.impl

import com.github.shynixn.mccoroutine.minestom.CoroutineSession
import com.github.shynixn.mccoroutine.minestom.MCCoroutineExceptionEvent
import com.github.shynixn.mccoroutine.minestom.dispatcher.AsyncCoroutineDispatcher
import com.github.shynixn.mccoroutine.minestom.dispatcher.MinecraftCoroutineDispatcher
import kotlinx.coroutines.*
import net.minestom.server.MinecraftServer
import net.minestom.server.event.EventDispatcher
import net.minestom.server.extensions.Extension
import kotlin.coroutines.CoroutineContext

internal class CoroutineSessionImpl(private val extension: Any) : CoroutineSession {
    /**
     * Gets minecraft coroutine scope.
     */
    override val scope: CoroutineScope

    /**
     * Gets the minecraft dispatcher.
     */
    override val dispatcherMinecraft: CoroutineContext by lazy {
        MinecraftCoroutineDispatcher()
    }

    /**
     * Gets the async dispatcher.
     */
    override val dispatcherAsync: CoroutineContext by lazy {
        AsyncCoroutineDispatcher()
    }

    init {
        // Root Exception Handler. All Exception which are not consumed by the caller end up here.
        val exceptionHandler = CoroutineExceptionHandler { _, e ->
            val mcCoroutineExceptionEvent = if (extension is Extension) {
                MCCoroutineExceptionEvent(extension, e)
            } else {
                MCCoroutineExceptionEvent(null, e)
            }

            MinecraftServer.getSchedulerManager().scheduleNextTick {
                EventDispatcher.call(mcCoroutineExceptionEvent)
                if (!mcCoroutineExceptionEvent.isCancelled) {
                    if (extension is Extension) {
                        extension.logger.error(
                            "This is not an error of MCCoroutine! See sub exception for details.",
                            e
                        )
                    } else {
                        MinecraftServer.LOGGER.error(
                            "This is not an error of MCCoroutine! See sub exception for details.",
                            e
                        )
                    }
                }
            }
        }

        // Build Coroutine plugin scope for exception handling
        val rootCoroutineScope = CoroutineScope(exceptionHandler)

        // Minecraft Scope is child of plugin scope and supervisor job (e.g. children of a supervisor job can fail independently).
        scope = rootCoroutineScope + SupervisorJob() + dispatcherMinecraft
    }

    /**
     * Disposes the session.
     */
    fun dispose() {
        scope.coroutineContext.cancelChildren()
        scope.cancel()
    }
}
