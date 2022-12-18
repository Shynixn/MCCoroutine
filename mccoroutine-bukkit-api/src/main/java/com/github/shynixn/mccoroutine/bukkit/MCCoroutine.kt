package com.github.shynixn.mccoroutine.bukkit

import kotlinx.coroutines.*
import org.bukkit.command.PluginCommand
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

/**
 * Static session for all plugins.
 */
internal val mcCoroutine: MCCoroutine by lazy {
    try {
        Class.forName("com.github.shynixn.mccoroutine.bukkit.impl.MCCoroutineImpl")
            .getDeclaredConstructor().newInstance() as MCCoroutine
    } catch (e: Exception) {
        throw RuntimeException(
            "Failed to load MCCoroutine implementation. Shade mccoroutine-bukkit-core into your plugin.",
            e
        )
    }
}


/**
 * Gets the configuration instance of MCCoroutine.
 */
val Plugin.mcCoroutineConfiguration : MCCoroutineConfiguration
    get() {
        return mcCoroutine.getCoroutineSession(this).mcCoroutineConfiguration
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
 * Launches a new coroutine on the minecraft main thread without blocking the current thread and returns a reference to the coroutine as a [Job].
 * The coroutine is cancelled when the resulting job is [cancelled][Job.cancel].
 *
 * The coroutine context is inherited from a [Plugin.scope]. Additional context elements can be specified with [context] argument.
 * If the context does not have any dispatcher nor any other [ContinuationInterceptor], then [Plugin.minecraftDispatcher] is used.
 * The parent job is inherited from a [Plugin.scope] as well, but it can also be overridden
 * with a corresponding [context] element.
 *
 * By default, the coroutine is immediately scheduled for execution if the current thread is already the minecraft server thread.
 * If the current thread is not the minecraft server thread, the coroutine is moved to the [org.bukkit.scheduler.BukkitScheduler] and executed
 * in the next server tick schedule.
 * Other start options can be specified via `start` parameter. See [CoroutineStart] for details.
 * An optional [start] parameter can be set to [CoroutineStart.LAZY] to start coroutine _lazily_. In this case,
 * the coroutine [Job] is created in _new_ state. It can be explicitly started with [start][Job.start] function
 * and will be started implicitly on the first invocation of [join][Job.join].
 *
 * Uncaught exceptions in this coroutine do not cancel the parent job or any other child jobs. All uncaught exceptions
 * are logged to [Plugin.getLogger] by default.
 *
 * @param context The coroutine context to start. Should almost be always be [Plugin.minecraftDispatcher]. Async operations should be
 * be created using [withContext] after using the default parameters of this method.
 * @param start coroutine start option. The default value is [CoroutineStart.DEFAULT].
 * @param block the coroutine code which will be invoked in the context of the provided scope.
 **/
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
 * Does exactly the same thing as PluginManager.registerEvents but makes suspend functions
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
    return mcCoroutine.getCoroutineSession(plugin).registerSuspendListener(listener)
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
    return callSuspendingEvent(event, plugin, EventExecutionType.Concurrent)
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
    eventExecutionType: EventExecutionType
): Collection<Job> {
    return mcCoroutine.getCoroutineSession(plugin).fireSuspendingEvent(event, eventExecutionType)
}

/**
 * Registers a command executor with suspending function.
 * Does exactly the same as PluginCommand.setExecutor.
 */
fun PluginCommand.setSuspendingExecutor(
    suspendingCommandExecutor: SuspendingCommandExecutor
) {
    return mcCoroutine.getCoroutineSession(plugin).registerSuspendCommandExecutor(
        plugin.minecraftDispatcher,
        this,
        suspendingCommandExecutor
    )
}

/**
 * Registers a command executor with suspending function.
 * Does exactly the same as PluginCommand.setExecutor.
 * @param context The coroutine context to start. Should almost be always be [Plugin.minecraftDispatcher].
 */
fun PluginCommand.setSuspendingExecutor(
    context: CoroutineContext,
    suspendingCommandExecutor: SuspendingCommandExecutor
) {
    return mcCoroutine.getCoroutineSession(plugin).registerSuspendCommandExecutor(
        context,
        this,
        suspendingCommandExecutor
    )
}

/**
 * Registers a tab completer with suspending function.
 * Does exactly the same as PluginCommand.setExecutor.
 */
fun PluginCommand.setSuspendingTabCompleter(suspendingTabCompleter: SuspendingTabCompleter) {
    return mcCoroutine.getCoroutineSession(plugin).registerSuspendTabCompleter(
        plugin.minecraftDispatcher,
        this,
        suspendingTabCompleter
    )
}


/**
 * Registers a tab completer with suspending function.
 * Does exactly the same as PluginCommand.setExecutor.
 * @param context The coroutine context to start. Should almost be always be [Plugin.minecraftDispatcher].
 */
fun PluginCommand.setSuspendingTabCompleter(context: CoroutineContext, suspendingTabCompleter: SuspendingTabCompleter) {
    return mcCoroutine.getCoroutineSession(plugin).registerSuspendTabCompleter(
        context,
        this,
        suspendingTabCompleter
    )
}

/**
 * Converts the number to ticks for being used together with delay(..).
 * E.g. delay(1.ticks).
 * Minecraft ticks 20 times per second, which means a tick appears every 50 milliseconds. However,
 * delay() does not directly work with the BukkitScheduler and needs millisecond manipulation to
 * work as expected. Therefore, 1 tick does not equal 50 milliseconds when using this method standalone and only
 * sums up to 50 milliseconds if you use it together with delay.
 */
val Int.ticks: Long
    get() {
        return (this * 50L - 25)
    }

/**
 * Hidden internal MCCoroutine interface.
 */
interface MCCoroutine {
    /**
     * Get coroutine session for the given plugin.
     */
    fun getCoroutineSession(plugin: Plugin): CoroutineSession

    /**
     * Disposes the given plugin.
     */
    fun disable(plugin: Plugin)
}
