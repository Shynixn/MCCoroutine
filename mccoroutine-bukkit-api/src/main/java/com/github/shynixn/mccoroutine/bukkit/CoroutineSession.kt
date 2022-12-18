package com.github.shynixn.mccoroutine.bukkit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.bukkit.command.PluginCommand
import org.bukkit.event.Event
import org.bukkit.event.Listener
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
     * Minecraft Dispatcher.
     */
    val dispatcherMinecraft: CoroutineContext

    /**
     * Async Dispatcher.
     */
    val dispatcherAsync: CoroutineContext

    /**
     * Manipulates the bukkit server heart beat on startup.
     */
    var isManipulatedServerHeartBeatEnabled: Boolean

    /**
     * MCCoroutine Facade.
     */
    val mcCoroutineConfiguration: MCCoroutineConfiguration

    /**
     * Registers a suspend command executor.
     */
    fun registerSuspendCommandExecutor(
        context: CoroutineContext,
        pluginCommand: PluginCommand,
        commandExecutor: SuspendingCommandExecutor
    )

    /**
     * Registers a suspend tab completer.
     */
    fun registerSuspendTabCompleter(
        context: CoroutineContext,
        pluginCommand: PluginCommand,
        tabCompleter: SuspendingTabCompleter
    )

    /**
     * Registers a suspend listener.
     */
    fun registerSuspendListener(listener: Listener)

    /**
     * Fires a suspending [event] with the given [eventExecutionType].
     * @return Collection of receiver jobs. May already be completed.
     */
    fun fireSuspendingEvent(
        event: Event,
        eventExecutionType: EventExecutionType
    ): Collection<Job>
}
