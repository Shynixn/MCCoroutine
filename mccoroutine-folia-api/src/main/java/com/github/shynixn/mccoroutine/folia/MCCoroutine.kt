package com.github.shynixn.mccoroutine.folia

import kotlinx.coroutines.*
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.command.PluginCommand
import org.bukkit.entity.Entity
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
        Class.forName(MCCoroutine.Driver)
            .getDeclaredConstructor().newInstance() as MCCoroutine
    } catch (e: Exception) {
        throw RuntimeException(
            "Failed to load MCCoroutine implementation. Shade mccoroutine-folia-core into your plugin.",
            e
        )
    }
}

/**
 * Gets the configuration instance of MCCoroutine.
 */
val Plugin.mcCoroutineConfiguration: MCCoroutineConfiguration
    get() {
        return mcCoroutine.getCoroutineSession(this).mcCoroutineConfiguration
    }

/**
 * Gets the dispatcher to perform edits on data that the global region owns, such as game rules, day time, weather, or to execute commands using the console command sender.
 * If Folia is not loaded, this falls back to the bukkit minecraftDispatcher.
 */
val Plugin.globalRegionDispatcher: CoroutineContext
    get() {
        return mcCoroutine.getCoroutineSession(this).dispatcherGlobalRegion
    }

/**
 * Gets the plugin async dispatcher.
 */
val Plugin.asyncDispatcher: CoroutineContext
    get() {
        return mcCoroutine.getCoroutineSession(this).dispatcherAsync
    }

/**
 * Gets the dispatcher to schedule tasks on the region that owns the entity.
 * If Folia is not loaded, this falls back to the bukkit minecraftDispatcher.
 */
fun Plugin.entityDispatcher(entity: Entity): CoroutineContext {
    return mcCoroutine.getCoroutineSession(this).getEntityDispatcher(entity)
}

/**
 * Gets the dispatcher to schedule tasks on a particular region.
 * If Folia is not loaded, this falls back to the bukkit minecraftDispatcher.
 */
fun Plugin.regionDispatcher(location: Location): CoroutineContext {
    return mcCoroutine.getCoroutineSession(this)
        .getRegionDispatcher(location.world!!, location.blockX shr 4, location.blockZ shr 4)
}

/**
 * Gets the dispatcher to schedule tasks on a particular region.
 * If Folia is not loaded, this falls back to the bukkit minecraftDispatcher.
 */
fun Plugin.regionDispatcher(world: World, chunkX: Int, chunkZ: Int): CoroutineContext {
    return mcCoroutine.getCoroutineSession(this)
        .getRegionDispatcher(world, chunkX, chunkZ)
}

/**
 * Gets the plugin coroutine scope.
 */
val Plugin.scope: CoroutineScope
    get() {
        return mcCoroutine.getCoroutineSession(this).scope
    }

/**
 * Launches a new coroutine on the current thread without blocking the current thread and returns a reference to the coroutine as a [Job].
 * The coroutine is cancelled when the resulting job is [cancelled][Job.cancel].
 *
 * The coroutine context is inherited from a [Plugin.scope]. Additional context elements can be specified with [context] argument.
 * If the context does not have any dispatcher nor any other [ContinuationInterceptor], then Unconfined Dispatcher is used.
 * The parent job is inherited from a [Plugin.scope] as well, but it can also be overridden
 * with a corresponding [context] element.
 *
 * By default, the coroutine is immediately scheduled on the current calling thread. However, manipulating global data, entities or locations
 * is not safe in this context. Use subsequent operations for this case e.g. withContext(plugin.entityDispatcher(entity)) {} or withContext(plugin.regionDispatcher(location)) {}
 * Other start options can be specified via `start` parameter. See [CoroutineStart] for details.
 * An optional [start] parameter can be set to [CoroutineStart.LAZY] to start coroutine _lazily_. In this case,
 * the coroutine [Job] is created in _new_ state. It can be explicitly started with [start][Job.start] function
 * and will be started implicitly on the first invocation of [join][Job.join].
 *
 * Uncaught exceptions in this coroutine do not cancel the parent job or any other child jobs. All uncaught exceptions
 * are logged to [Plugin.getLogger] by default.
 *
 * @param context The coroutine context to start. As the context of the current operation cannot be assumed automatically, the caller needs to specify a context.
 * e.g. regionDispatcher, entityDispatcher or globalRegionDispatcher.
 * @param start coroutine start option. The default value is [CoroutineStart.DEFAULT].
 * @param block the coroutine code which will be invoked in the context of the provided scope.
 **/
fun Plugin.launch(
    context: CoroutineContext,
    start: CoroutineStart,
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
 * @param eventDispatcher Folia uses different schedulers for different event types. MCCoroutine cannot detect them per default and requires a mapping for
 * each used event type in the given [listener]. This method throws an exception if you forget to map an event type. See wiki for details.
 */
fun PluginManager.registerSuspendingEvents(
    listener: Listener,
    plugin: Plugin,
    eventDispatcher: Map<Class<out Event>, (event: Event) -> CoroutineContext>
) {
    return mcCoroutine.getCoroutineSession(plugin).registerSuspendListener(listener, eventDispatcher)
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
        plugin.globalRegionDispatcher,
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
        plugin.globalRegionDispatcher,
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
 * delay() does not directly work with the FoliaScheduler and needs millisecond manipulation to
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
    companion object {
        /**
         * Allows to change the driver to load different kinds of MCCoroutine implementations.
         * e.g. loading the implementation for UnitTests.
         */
        var Driver: String = "com.github.shynixn.mccoroutine.folia.impl.MCCoroutineImpl"
    }

    /**
     * Get coroutine session for the given plugin.
     */
    fun getCoroutineSession(plugin: Plugin): CoroutineSession

    /**
     * Disposes the given plugin.
     */
    fun disable(plugin: Plugin)
}
