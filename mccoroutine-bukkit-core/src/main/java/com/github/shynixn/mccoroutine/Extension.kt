package com.github.shynixn.mccoroutine

import com.github.shynixn.mccoroutine.entity.MCCoroyutineImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.bukkit.command.CommandExecutor
import org.bukkit.command.PluginCommand
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import java.lang.reflect.Method
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

/**
 * Static session.
 */
private val mcCoroutine = MCCoroyutineImpl()

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
 * Launches the given function in the Coroutine Scope of the given plugin.
 * This function may be called immediately without any delay if the Thread
 * calling this function Bukkit.isPrimaryThread() is true. This means
 * for example that event cancelling or modifying return values is still possible.
 * @param f callback function inside a coroutine scope.
 */
fun Plugin.launchMinecraft(f: suspend CoroutineScope.() -> Unit) {
    mcCoroutine.getCoroutineSession(this).launchOnMinecraft(f)
}

/**
 * Launches the given function in the Coroutine Scope of the given plugin async.
 * This function may be called immediately without any delay if the Thread
 * calling this function Bukkit.isPrimaryThread() is false. This means
 * for example that event cancelling or modifying return values is still possible.
 * @param f callback function inside a coroutine scope.
 */
fun Plugin.launchAsync(f: suspend CoroutineScope.() -> Unit) {
    mcCoroutine.getCoroutineSession(this).launchOnAsync(f)
}

/**
 * Registers a flow of of the given event type. Makes event listening more flexible than Listener
 * implementation and does not use any kind of reflection.
 *
 * Example:
 *
 *  plugin.server.pluginManager
 *       .registerSuspendingEventFlow(PlayerJoinEvent::class.java, plugin)
 *       .collect {
 *           println(it.eventName)
 *       }
 *
 * @param event Event clazz.
 * @param plugin Bukkit Plugin.
 */
fun <T : Event> PluginManager.registerSuspendingEventFlow(
    event: Class<T>,
    plugin: Plugin,
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false
): Flow<T> {
    return mcCoroutine.getCoroutineSession(plugin).eventService.createEventFlow(event, priority, ignoreCancelled)
}

/**
 * Registers a flow of of the given event type. Makes event listening more flexible than Listener
 * implementation and does not use any kind of reflection.
 *
 * Example:
 *
 *  plugin.server.pluginManager
 *       .registerSuspendingEventFlow(PlayerJoinEvent::class.java, plugin)
 *       .collect {
 *           println(it.eventName)
 *       }
 *
 * @param event Event clazz.
 * @param plugin Bukkit Plugin.
 */
fun <T : Event> PluginManager.registerSuspendingEventFlow(
    event: KClass<T>,
    plugin: Plugin,
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false
): Flow<T> {
    return registerSuspendingEventFlow(event.java, plugin, priority, ignoreCancelled)
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
 * Registers a flow of for the command. Makes command listening more flexible than CommandExecutor
 * implementation.
 */
fun PluginCommand.registerSuspendingCommandFlow(): Flow<CommandEvent> {
    return mcCoroutine.getCoroutineSession(plugin).commandService.createCommandFlow(this)
}

/**
 * Internal reflection suspend.
 */
internal suspend fun Method.invokeSuspend(obj: Any, vararg args: Any?): Any? =
    kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn { cont ->
        invoke(obj, *args, cont)
    }

