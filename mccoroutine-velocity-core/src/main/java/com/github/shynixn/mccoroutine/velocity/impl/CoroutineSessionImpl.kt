package com.github.shynixn.mccoroutine.velocity.impl

import com.github.shynixn.mccoroutine.velocity.CoroutineSession
import com.github.shynixn.mccoroutine.velocity.MCCoroutineExceptionEvent
import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.github.shynixn.mccoroutine.velocity.dispatcher.VelocityCoroutineDispatcher
import com.github.shynixn.mccoroutine.velocity.service.CommandServiceImpl
import com.velocitypowered.api.command.Command
import com.velocitypowered.api.command.CommandMeta
import com.velocitypowered.api.plugin.PluginContainer
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

internal class CoroutineSessionImpl(
    private val pluginContainer: PluginContainer,
    private val suspendingPluginContainer: SuspendingPluginContainer
) : CoroutineSession {
    private val commandService: CommandServiceImpl by lazy {
        CommandServiceImpl(pluginContainer, suspendingPluginContainer)
    }

    /**
     * Gets the scope.
     */
    override val scope: CoroutineScope

    /**
     * Velocity Dispatcher.
     */
    override val dispatcherVelocity: CoroutineContext by lazy {
        VelocityCoroutineDispatcher(pluginContainer, suspendingPluginContainer)
    }

    init {
        // Root Exception Handler. All Exception which are not consumed by the caller end up here.
        val exceptionHandler = CoroutineExceptionHandler { _, e ->
            val mcCoroutineExceptionEvent = MCCoroutineExceptionEvent(pluginContainer, e)
            suspendingPluginContainer.server.scheduler
                .buildTask(pluginContainer, Runnable {
                    suspendingPluginContainer.server.eventManager.fire(mcCoroutineExceptionEvent)
                        .thenAccept { resultEvent ->
                            if (resultEvent.result.isAllowed) {
                                if (e !is CancellationException) {
                                    suspendingPluginContainer.logger.error(
                                        "This is not an error of MCCoroutine! See sub exception for details.",
                                        e
                                    )
                                }
                            }
                        }
                })
                .schedule()
        }

        // Build Coroutine plugin scope for exception handling
        val rootCoroutineScope = CoroutineScope(exceptionHandler)

        // Velocity Scope is child of plugin scope and super visor job (e.g. children of a supervisor job can fail independently).
        scope = rootCoroutineScope + SupervisorJob() + dispatcherVelocity
    }


    /**
     * Registers a suspend listener.
     */
    override fun registerSuspendListener(listener: Any) {
        TODO("Not yet implemented")
    }

    /**
     * Registers a suspend command.
     */
    override fun registerSuspendCommand(meta: CommandMeta?, command: Command) {
        commandService.registerCommand(meta!!, command)
    }

    /**
     * Disposes the session.
     */
    fun dispose() {
        scope.coroutineContext.cancelChildren()
        scope.cancel()
    }
}
