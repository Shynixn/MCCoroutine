package com.github.shynixn.mccoroutine

import com.github.shynixn.mccoroutine.contract.MCCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import net.md_5.bungee.api.plugin.*
import kotlin.coroutines.CoroutineContext

/**
 * Static session.
 */
internal val mcCoroutine: MCCoroutine by lazy {
    try {
        Class.forName("com.github.shynixn.mccoroutine.impl.MCCoroutineImpl")
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
 * Gets the plugin async dispatcher.
 */
val Plugin.asyncDispatcher: CoroutineContext
    get() {
        return Dispatchers.IO
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
 * The given function is always called later regardless of the current threading context.
 * @param dispatcher Coroutine context. The default context is bungeeCord dispatcher.
 * @param f callback function inside a coroutine scope.
 * @return Cancelable coroutine job.
 */
fun Plugin.launch(dispatcher: CoroutineContext, f: suspend CoroutineScope.() -> Unit): Job {
    return mcCoroutine.getCoroutineSession(this).launch(dispatcher, f)
}

/**
 * Launches the given function in the Coroutine Scope of the given plugin.
 * The given function is always called later regardless of the current threading context.
 * The context is bungeeCord dispatcher.
 * @param f callback function inside a coroutine scope.
 * @return Cancelable coroutine job.
 */
fun Plugin.launch(f: suspend CoroutineScope.() -> Unit): Job {
    return mcCoroutine.getCoroutineSession(this).launch(bungeeCordDispatcher, f)
}

/**
 * Launches the given function in the Coroutine Scope of the given plugin async.
 * The given function is always called later regardless of the current threading context.
 * The context is the default IO dispatcher.
 * @param f callback function inside a coroutine scope.
 * @return Cancelable coroutine job.
 */
fun Plugin.launchAsync(f: suspend CoroutineScope.() -> Unit): Job {
    return mcCoroutine.getCoroutineSession(this).launch(this.asyncDispatcher, f)
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
    return mcCoroutine.getCoroutineSession(plugin).eventService.registerSuspendListener(listener)
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
    return mcCoroutine.getCoroutineSession(plugin).commandService.registerSuspendCommandExecutor(command)
}
