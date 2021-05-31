package com.github.shynixn.mccoroutine

import com.github.shynixn.mccoroutine.contract.MCCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
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
        Class.forName("com.github.shynixn.mccoroutine.impl.MCCoroutineImpl")
            .newInstance() as MCCoroutine
    } catch (e: Exception) {
        throw RuntimeException(
            "Failed to load MCCoroutine implementation. Shade mccoroutine-bukkit-core into your plugin.",
            e
        )
    }
}

private var serverVersionInternal: String? = null

/**
 * Gets the server NMS version.
 */
val Plugin.serverVersion: String
    get() {
        if (serverVersionInternal == null) {
            serverVersionInternal = server.javaClass.getPackage().name.replace(".", ",").split(",")[3]
        }

        return serverVersionInternal!!
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
fun Plugin.launch(dispatcher: CoroutineContext, f: suspend CoroutineScope.() -> Unit): Job {
    return mcCoroutine.getCoroutineSession(this).launch(dispatcher, f)
}

/**
 * Launches the given function in the Coroutine Scope of the given plugin.
 * This function may be called immediately without any delay if the Thread
 * calling this function Bukkit.isPrimaryThread() is true. This means
 * for example that event cancelling or modifying return values is still possible.
 * @param f callback function inside a coroutine scope.
 * @return Cancelable coroutine job.
 */
fun Plugin.launch(f: suspend CoroutineScope.() -> Unit): Job {
    return mcCoroutine.getCoroutineSession(this).launch(minecraftDispatcher, f)
}

/**
 * Launches the given function in the Coroutine Scope of the given plugin async.
 * This function may be called immediately without any delay if the Thread
 * calling this function Bukkit.isPrimaryThread() is false. This means
 * for example that event cancelling or modifying return values is still possible.
 * @param f callback function inside a coroutine scope.
 * @return Cancelable coroutine job.
 */
fun Plugin.launchAsync(f: suspend CoroutineScope.() -> Unit): Job {
    return mcCoroutine.getCoroutineSession(this).launch(this.asyncDispatcher, f)
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
 * Allows to await the completion of suspending event listeners.
 *
 * @param event Event details.
 * @param plugin Plugin plugin.
 * @return Collection of awaitable jobs. This job list may be empty if no suspending listener
 * was called. Each job instance represents an awaitable job for each method being called in each suspending listener.
 * For awaiting use callSuspendingEvent(..).joinAll().
 */
fun PluginManager.callSuspendingEvent(event: Event, plugin: Plugin): Collection<Job> {
    return mcCoroutine.getCoroutineSession(plugin).eventService.fireSuspendingEvent(event)
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

/**
 * Finds the version compatible class.
 */
fun Plugin.findClazz(name: String): Class<*> {
    return Class.forName(
        name.replace(
            "VERSION",
            serverVersion
        )
    )
}
