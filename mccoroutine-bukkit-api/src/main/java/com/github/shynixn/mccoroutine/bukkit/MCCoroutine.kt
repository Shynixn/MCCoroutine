package com.github.shynixn.mccoroutine.bukkit

import com.github.shynixn.mccoroutine.bukkit.internal.MCCoroutine
import kotlinx.coroutines.*
import org.bukkit.command.PluginCommand
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import kotlin.coroutines.CoroutineContext

/**
 * Static session.
 */
internal val mcCoroutine: MCCoroutine by lazy {
    try {
        Class.forName("com.github.shynixn.mccoroutine.bukkit.impl.MCCoroutineImpl")
            .newInstance() as MCCoroutine
    } catch (e: Exception) {
        throw RuntimeException(
            "Failed to load MCCoroutine implementation. Shade mccoroutine-bukkit-core into your plugin.",
            e
        )
    }
}

/**
 * Gets the plugin minecraft dispatcher.
 */
val Plugin.minecraftDispatcher: CoroutineContext
    get() {
        return mcCoroutine.getCoroutineSession(this).dispatcherMinecraft
    }

/**
 * Gets the plugin async dispatcher.
 */
val Plugin.asyncDispatcher: CoroutineContext
    get() {
        return mcCoroutine.getCoroutineSession(this).dispatcherAsync
    }

/**
 * Gets the plugin coroutine scope.
 */
val Plugin.scope: CoroutineScope
    get() {
        return mcCoroutine.getCoroutineSession(this).scope
    }

/**
 * Launches the given function in the Coroutine Scope of the given plugin.
 * This function may be called immediately without any delay if the Thread
 * calling this function Bukkit.isPrimaryThread() is true. This means
 * for example that event cancelling or modifying return values is still possible.
 * @param dispatcher Coroutine context. The default context is minecraft dispatcher.
 * @param f callback function inside a coroutine scope.
 * @return Cancelable coroutine job.
 */
fun Plugin.launch(
    context: CoroutineContext = minecraftDispatcher,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    if (!scope.isActive) {
        return Job()
    }

    return scope.launch(context, start, block)
}

/**
 * Registers an event listener with suspending functions.
 * Does exactly the same thing as PluginManager.registerEvents but makes suspension functions
 * possible.
 * Example:
 *
 * class MyPlayerJoinListener : Listener{
 *     @EventHandler
 *     suspend fun onPlayerJoinEvent(event: PlayerJoinEvent) {
 *
 *     }
 * }
 *
 * @param listener Bukkit Listener.
 * @param plugin Bukkit Plugin.
 */
fun PluginManager.registerSuspendingEvents(listener: Listener, plugin: Plugin) {
    return mcCoroutine.getCoroutineSession(plugin).eventService.registerSuspendListener(listener)
}

/**
 * Calls an event with the given details.
 * If there are multiple suspend event receivers, each receiver is executed concurrently.
 * Allows to await the completion of suspending event listeners.
 *
 * @param event Event details.
 * @param plugin Plugin plugin.
 * @return Collection of awaitable jobs. This job list may be empty if no suspending listener
 * was called. Each job instance represents an awaitable job for each method being called in each suspending listener.
 * For awaiting use callSuspendingEvent(..).joinAll().
 */
fun PluginManager.callSuspendingEvent(event: Event, plugin: Plugin): Collection<Job> {
    return callSuspendingEvent(event, plugin, com.github.shynixn.mccoroutine.bukkit.EventExecutionType.Concurrent)
}

/**
 * Calls an event with the given details.
 * Allows to await the completion of suspending event listeners.
 *
 * @param event Event details.
 * @param plugin Plugin plugin.
 * @param eventExecutionType Allows to specify how suspend receivers are executed.
 * @return Collection of awaitable jobs. This job list may be empty if no suspending listener
 * was called. Each job instance represents an awaitable job for each method being called in each suspending listener.
 * For awaiting use callSuspendingEvent(..).joinAll().
 */
fun PluginManager.callSuspendingEvent(
    event: Event,
    plugin: Plugin,
    eventExecutionType: com.github.shynixn.mccoroutine.bukkit.EventExecutionType
): Collection<Job> {
    return mcCoroutine.getCoroutineSession(plugin).eventService.fireSuspendingEvent(event, eventExecutionType)
}

/**
 * Registers an command executor with suspending function.
 * Does exactly the same as PluginCommand.setExecutor.
 */
fun PluginCommand.setSuspendingExecutor(
    suspendingCommandExecutor: SuspendingCommandExecutor
) {
    return mcCoroutine.getCoroutineSession(plugin).commandService.registerSuspendCommandExecutor(
        this,
        suspendingCommandExecutor
    )
}

/**
 * Registers a tab completer with suspending function.
 * Does exactly the same as PluginCommand.setExecutor.
 */
fun PluginCommand.setSuspendingTabCompleter(suspendingTabCompleter: SuspendingTabCompleter) {
    return mcCoroutine.getCoroutineSession(plugin).commandService.registerSuspendTabCompleter(
        this,
        suspendingTabCompleter
    )
}
