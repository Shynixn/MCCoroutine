package com.github.shynixn.mccoroutine.sponge

import kotlinx.coroutines.*
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.event.Event
import org.spongepowered.api.event.EventManager
import org.spongepowered.api.plugin.PluginContainer
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

/**
 * Static session for all plugins.
 */
internal val mcCoroutine: MCCoroutine by lazy {
    try {
        Class.forName("com.github.shynixn.mccoroutine.sponge.impl.MCCoroutineImpl")
            .newInstance() as MCCoroutine
    } catch (e: Exception) {
        throw RuntimeException(
            "Failed to load MCCoroutine implementation. Shade mccoroutine-sponge-core into your plugin.",
            e
        )
    }
}

/**
 * Gets the plugin minecraft dispatcher.
 */
val PluginContainer.minecraftDispatcher: CoroutineContext
    get() {
        return mcCoroutine.getCoroutineSession(this).dispatcherMinecraft
    }

/**
 * Gets the plugin async dispatcher.
 */
val PluginContainer.asyncDispatcher: CoroutineContext
    get() {
        return mcCoroutine.getCoroutineSession(this).dispatcherAsync
    }

/**
 * Gets the plugin coroutine scope.
 */
val PluginContainer.scope: CoroutineScope
    get() {
        return mcCoroutine.getCoroutineSession(this).scope
    }

/**
 * Launches a new coroutine on the minecraft main thread without blocking the current thread and returns a reference to the coroutine as a [Job].
 * The coroutine is cancelled when the resulting job is [cancelled][Job.cancel].
 *
 * The coroutine context is inherited from a [PluginContainer.scope]. Additional context elements can be specified with [context] argument.
 * If the context does not have any dispatcher nor any other [ContinuationInterceptor], then [Plugin.minecraftDispatcher] is used.
 * The parent job is inherited from a [PluginContainer.scope] as well, but it can also be overridden
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
 * are logged to a customer logger by default.
 *
 * @param context The coroutine context to start. Should almost be always be [Plugin.minecraftDispatcher]. Async operations should be
 * be created using [withContext] after using the default parameters of this method.
 * @param start coroutine start option. The default value is [CoroutineStart.DEFAULT].
 * @param block the coroutine code which will be invoked in the context of the provided scope.
 **/
fun PluginContainer.launch(
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
fun EventManager.registerSuspendingListeners(plugin: PluginContainer, listener: Any) {
    return mcCoroutine.getCoroutineSession(plugin).registerSuspendListener(listener)
}

/**
 * Calls an event with the given details.
 * If there are multiple suspend event receivers, each receiver is executed concurrently.
 * Allows to await the completion of suspending event listeners.
 *
 * @param event Event details.
 * @param plugin PluginContainer plugin.
 * @return Collection of awaitable jobs. This job list may be empty if no suspending listener
 * was called. Each job instance represents an awaitable job for each method being called in each suspending listener.
 * For awaiting use callSuspendingEvent(..).joinAll().
 */
fun EventManager.postSuspending(event: Event, plugin: PluginContainer): Collection<Job> {
    return postSuspending(event, plugin, EventExecutionType.Concurrent)
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

fun EventManager.postSuspending(
    event: Event,
    plugin: PluginContainer,
    eventExecutionType: EventExecutionType
): Collection<Job> {
    return mcCoroutine.getCoroutineSession(plugin).fireSuspendingEvent(event, eventExecutionType)
}

/**
 * Registers an command executor with suspending function.
 * Does exactly the same as PluginCommand.setExecutor.
 */
fun CommandSpec.Builder.suspendingExecutor(
    plugin: PluginContainer,
    suspendingCommandExecutor: SuspendingCommandExecutor
): CommandSpec.Builder {
    mcCoroutine.getCoroutineSession(plugin).registerSuspendCommandExecutor(
        this,
        suspendingCommandExecutor
    )
    return this
}

/**
 * Converts the number to ticks for being used together with delay(..).
 * E.g. delay(1.ticks).
 * Minecraft ticks 20 times per second, which means a tick appears every 50 milliseconds. However,
 * delay() does not directly work with the SpongeScheduler and needs millisecond manipulation to
 * work as expected. Therefore, 1 tick does not equal 50 milliseconds when using this method standalone and only
 * sums up to 50 milliseconds if you use it together with delay.
 */
val Int.ticks: Long
    get() {
        return (this * 50L - 25)
    }

interface MCCoroutine {
    /**
     * Get coroutine session for the given plugin.
     */
    fun getCoroutineSession(plugin: PluginContainer): CoroutineSession

    /**
     * Disposes the given plugin.
     */
    fun disable(plugin: PluginContainer)
}
