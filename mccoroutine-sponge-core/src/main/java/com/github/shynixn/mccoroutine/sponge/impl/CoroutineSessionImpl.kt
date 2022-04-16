package com.github.shynixn.mccoroutine.sponge.impl

import com.github.shynixn.mccoroutine.sponge.CoroutineSession
import com.github.shynixn.mccoroutine.sponge.EventExecutionType
import com.github.shynixn.mccoroutine.sponge.MCCoroutineExceptionEvent
import com.github.shynixn.mccoroutine.sponge.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.sponge.dispatcher.AsyncCoroutineDispatcher
import com.github.shynixn.mccoroutine.sponge.dispatcher.MinecraftCoroutineDispatcher
import com.github.shynixn.mccoroutine.sponge.service.CommandServiceImpl
import com.github.shynixn.mccoroutine.sponge.service.EventServiceImpl
import kotlinx.coroutines.*
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.event.Event
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.scheduler.Task
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext

internal class CoroutineSessionImpl(private val plugin: PluginContainer) :
    CoroutineSession {
    private val logger = Logger.getLogger("MCCoroutine-" + plugin.name)
    private val eventService: EventServiceImpl by lazy {
        EventServiceImpl(plugin, logger)
    }
    private val commandServiceImpl: CommandServiceImpl by lazy {
        CommandServiceImpl(plugin)
    }

    /**
     * Gets the scope.
     */
    override val scope: CoroutineScope

    /**
     * Gets the minecraft dispatcher.
     */
    override val dispatcherMinecraft: CoroutineContext by lazy {
        MinecraftCoroutineDispatcher(plugin)
    }

    /**
     * Gets the async dispatcher.
     */
    override val dispatcherAsync: CoroutineContext by lazy {
        AsyncCoroutineDispatcher(plugin)
    }

    init {
        // Root Exception Handler. All Exception which are not consumed by the caller end up here.
        val exceptionHandler = CoroutineExceptionHandler { _, e ->
            val mcCoroutineExceptionEvent = MCCoroutineExceptionEvent(plugin, e)

            Task.builder()
                .execute(Runnable {
                    Sponge.getEventManager().post(mcCoroutineExceptionEvent)

                    if (!mcCoroutineExceptionEvent.isCancelled) {
                        if (e !is CancellationException) {
                            logger.log(
                                Level.SEVERE,
                                "This is not an error of MCCoroutine! See sub exception for details.",
                                e
                            )
                        }
                    }
                }).submit(plugin)
        }

        // Build Coroutine plugin scope for exception handling
        val rootCoroutineScope = CoroutineScope(exceptionHandler)

        // Minecraft Scope is child of plugin scope and super visor job (e.g. children of a supervisor job can fail independently).
        scope = rootCoroutineScope + SupervisorJob() + dispatcherMinecraft
    }

    /**
     * Registers a suspend command executor.
     */
    override fun registerSuspendCommandExecutor(
        commandSpec: CommandSpec.Builder,
        commandExecutor: SuspendingCommandExecutor
    ) {
        commandServiceImpl.registerSuspendCommandExecutor(commandSpec, commandExecutor)
    }

    /**
     * Registers a suspend listener.
     */
    override fun registerSuspendListener(listener: Any) {
        eventService.registerSuspendListener(listener)
    }

    /**
     * Fires a suspending [event] with the given [eventExecutionType].
     * @return Collection of receiver jobs. May already be completed.
     */
    override fun fireSuspendingEvent(event: Event, eventExecutionType: EventExecutionType): Collection<Job> {
        return eventService.fireSuspendingEvent(event, eventExecutionType)
    }

    /**
     * Disposes the session.
     */
    fun dispose() {
        scope.coroutineContext.cancelChildren()
        scope.cancel()
    }
}
