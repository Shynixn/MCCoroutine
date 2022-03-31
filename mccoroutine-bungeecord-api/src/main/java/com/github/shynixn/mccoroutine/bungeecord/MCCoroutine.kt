package com.github.shynixn.mccoroutine.bungeecord

import kotlinx.coroutines.*
import net.md_5.bungee.api.plugin.*
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

/**
 * Static session.
 */
internal val mcCoroutine: MCCoroutine by lazy {
    try {
        Class.forName("com.github.shynixn.mccoroutine.bungeecord.impl.MCCoroutineImpl")
            .newInstance() as MCCoroutine
    } catch (e: Exception) {
        throw RuntimeException(
            "Failed to load MCCoroutine implementation. Shade mccoroutine-bungeecord-core into your plugin.",
            e
        )
    }
}

/**
 * Gets the plugin bungeeCord dispatcher.
 */
val Plugin.bungeeCordDispatcher: CoroutineContext
    get() {
        return mcCoroutine.getCoroutineSession(this).dispatcherBungeeCord
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
 * are logged to [Plugin.getLogger] by default. If you w
 *
 * @param context The coroutine context to start. Should almost be always be [Plugin.minecraftDispatcher]. Async operations should be
 * be created using [withContext] after using the default parameters of this method.
 * @param start coroutine start option. The default value is [CoroutineStart.DEFAULT].
 * @param block the coroutine code which will be invoked in the context of the provided scope.
 **/
fun Plugin.launch(
    context: CoroutineContext = bungeeCordDispatcher,
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
 * Does exactly the same thing as PluginManager.registerListener but makes suspension functions
 * possible.
 * Example:
 *
 * class MyListener : Listener{
 *     @EventHandler
 *     suspend fun onPostLoginEvent(event: PostLoginEvent) {
 *
 *     }
 * }
 *
 * @param listener BungeeCord Listener.
 * @param plugin BungeeCord Plugin.
 */
fun PluginManager.registerSuspendingListener(plugin: Plugin, listener: Listener) {
    return mcCoroutine.getCoroutineSession(plugin).registerSuspendListener(listener)
}

/**
 * Registers an command executor with suspending function.
 * Does exactly the same as PluginManager.registerCommand
 * @param plugin BungeeCord Plugin.
 * @see command SuspendingCommand
 */
fun PluginManager.registerSuspendingCommand(
    plugin: Plugin,
    command: SuspendingCommand
) {
    command.command.plugin = plugin
    plugin.proxy.pluginManager.registerCommand(plugin, command.command)
}

interface MCCoroutine {
    /**
     * Get coroutine session for the given plugin.
     */
    fun getCoroutineSession(plugin: Plugin): CoroutineSession

    /**
     * Disables coroutine for the given plugin.
     */
    fun disable(plugin: Plugin)
}
