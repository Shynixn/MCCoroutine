package com.github.shynixn.mccoroutine.bukkit.impl

import com.github.shynixn.mccoroutine.bukkit.*
import com.github.shynixn.mccoroutine.bukkit.dispatcher.AsyncCoroutineDispatcher
import com.github.shynixn.mccoroutine.bukkit.dispatcher.MinecraftCoroutineDispatcher
import com.github.shynixn.mccoroutine.bukkit.service.CommandServiceImpl
import com.github.shynixn.mccoroutine.bukkit.service.EventServiceImpl
import com.github.shynixn.mccoroutine.bukkit.service.WakeUpBlockServiceImpl
import kotlinx.coroutines.*
import org.bukkit.command.PluginCommand
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext

internal class CoroutineSessionImpl(private val plugin: Plugin, override val mcCoroutineConfiguration : MCCoroutineConfiguration) : CoroutineSession {
    /**
     * Gets the block service during startup.
     */
    private val wakeUpBlockService: WakeUpBlockServiceImpl by lazy {
        WakeUpBlockServiceImpl(plugin)
    }

    /**
     * Gets the event service.
     */
    private val eventService: EventServiceImpl by lazy {
        EventServiceImpl(plugin)
    }

    /**
     * Gets the command service.
     */
    private val commandService: CommandServiceImpl by lazy {
        CommandServiceImpl(plugin)
    }

    /**
     * Gets minecraft coroutine scope.
     */
    override val scope: CoroutineScope

    /**
     * Gets the minecraft dispatcher.
     */
    override val dispatcherMinecraft: CoroutineContext by lazy {
        MinecraftCoroutineDispatcher(plugin, wakeUpBlockService)
    }

    /**
     * Gets the async dispatcher.
     */
    override val dispatcherAsync: CoroutineContext by lazy {
        AsyncCoroutineDispatcher(plugin, wakeUpBlockService)
    }

    /**
     * Manipulates the bukkit server heart beat on startup.
     */
    override var isManipulatedServerHeartBeatEnabled: Boolean
        get() {
            return wakeUpBlockService.isManipulatedServerHeartBeatEnabled
        }
        set(value) {
            wakeUpBlockService.isManipulatedServerHeartBeatEnabled = value
        }

    init {
        // Root Exception Handler. All Exception which are not consumed by the caller end up here.
        val exceptionHandler = CoroutineExceptionHandler { _, e ->
            val mcCoroutineExceptionEvent = MCCoroutineExceptionEvent(plugin, e)

            if (plugin.isEnabled) {
                plugin.server.scheduler.runTask(plugin, Runnable {
                    plugin.server.pluginManager.callEvent(mcCoroutineExceptionEvent)

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
        context: CoroutineContext,
        pluginCommand: PluginCommand,
        commandExecutor: SuspendingCommandExecutor
    ) {
        commandService.registerSuspendCommandExecutor(context, pluginCommand, commandExecutor)
    }

    /**
     * Registers a suspend tab completer.
     */
    override fun registerSuspendTabCompleter(
        context: CoroutineContext,
        pluginCommand: PluginCommand,
        tabCompleter: SuspendingTabCompleter
    ) {
        commandService.registerSuspendTabCompleter(context, pluginCommand, tabCompleter)
    }

    /**
     * Registers a suspend listener.
     */
    override fun registerSuspendListener(listener: Listener) {
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
        wakeUpBlockService.dispose()
    }
}
