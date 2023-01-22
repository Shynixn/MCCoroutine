package com.github.shynixn.mccoroutine.fabric.impl

import com.github.shynixn.mccoroutine.fabric.CoroutineSession
import com.github.shynixn.mccoroutine.fabric.MCCoroutineConfiguration
import com.github.shynixn.mccoroutine.fabric.MCCoroutineExceptionEvent
import com.github.shynixn.mccoroutine.fabric.dispatcher.MinecraftCoroutineDispatcher
import kotlinx.coroutines.*
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext

internal class CoroutineSessionImpl(
    private val extension: Any,
    override val mcCoroutineConfiguration: MCCoroutineConfiguration
) : CoroutineSession {
    /**
     * Gets minecraft coroutine scope.
     */
    override val scope: CoroutineScope by lazy {
        // Root Exception Handler. All Exception which are not consumed by the caller end up here.
        val exceptionHandler = CoroutineExceptionHandler { _, e ->
            mcCoroutineConfiguration.minecraftExecutor.execute {
                val isCancelled = MCCoroutineExceptionEvent.EVENT.invoker().onMCCoroutineException(e, extension)
                if (!isCancelled) {
                    mcCoroutineConfiguration.logger.log(
                        Level.SEVERE, "This is not an error of MCCoroutine! See sub exception for details.",
                        e
                    )
                }
            }
        }

        // Build Coroutine plugin scope for exception handling
        val rootCoroutineScope = CoroutineScope(exceptionHandler)

        // Minecraft Scope is child of plugin scope and supervisor job (e.g. children of a supervisor job can fail independently).
        rootCoroutineScope + SupervisorJob() + dispatcherMinecraft
    }

    /**
     * Gets the minecraft dispatcher.
     */
    override val dispatcherMinecraft: CoroutineContext by lazy {
        MinecraftCoroutineDispatcher(mcCoroutineConfiguration.minecraftExecutor)
    }

    /**
     * Disposes the session.
     */
    fun dispose() {
        scope.coroutineContext.cancelChildren()
        scope.cancel()
    }
}
