package com.github.shynixn.mccoroutine

import com.github.shynixn.mccoroutine.contract.MCCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.event.Event
import org.spongepowered.api.event.EventManager
import org.spongepowered.api.plugin.PluginContainer
import kotlin.coroutines.CoroutineContext

/**
 * Static session.
 */
private val mcCoroutine: MCCoroutine by lazy {
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


private var serverVersionInternal: String? = null

/**
 * Gets the server NMS version.
 */
val PluginContainer.serverVersion: String
    get() {
        if (serverVersionInternal == null) {
            serverVersionInternal = "v1_12_R1"
        }

        return serverVersionInternal!!
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
 * Launches the given function in the Coroutine Scope of the given plugin.
 * This function may be called immediately without any delay if the Thread
 * calling this function Bukkit.isPrimaryThread() is true. This means
 * for example that event cancelling or modifying return values is still possible.
 * @param dispatcher Coroutine context. The default context is minecraft dispatcher.
 * @param f callback function inside a coroutine scope.
 * @return Cancelable coroutine job.
 */
fun PluginContainer.launch(dispatcher: CoroutineContext, f: suspend CoroutineScope.() -> Unit): Job {
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
fun PluginContainer.launch(f: suspend CoroutineScope.() -> Unit): Job {
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
fun PluginContainer.launchAsync(f: suspend CoroutineScope.() -> Unit): Job {
    return mcCoroutine.getCoroutineSession(this).launch(this.asyncDispatcher, f)
}

/**
 * Registers an event listener with suspending functions.
 * Does exactly the same thing as Sponge.getEventManager().registerListeners but makes suspension functions
 * possible.
 * Example:
 *
 * class MyPlayerJoinListener : Listener{
 *     @Listener
 *     suspend fun onPlayerJoinEvent(event: ClientConnectionEvent.Join) {
 *
 *     }
 * }
 *
 * @param plugin Sponge Plugin.
 * @param listener Sponge Listener.
 */
fun EventManager.registerSuspendingListeners(plugin: PluginContainer, listener: Any) {
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
 * For awaiting use postEvent(..).joinAll().
 */
fun EventManager.postSuspending(event: Event, plugin: PluginContainer): Collection<Job> {
    return mcCoroutine.getCoroutineSession(plugin).eventService.fireSuspendingEvent(event)
}

/**
 * Registers an command executor with suspending function.
 * Does exactly the same as CommandSpec.Builder.executor().
 */
fun CommandSpec.Builder.suspendingExecutor(
    plugin: PluginContainer,
    suspendingCommandExecutor: SuspendingCommandExecutor
): CommandSpec.Builder {
    mcCoroutine.getCoroutineSession(plugin).commandService.registerSuspendCommandExecutor(
        this,
        suspendingCommandExecutor
    )
    return this
}
