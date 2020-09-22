package com.github.shynixn.mccoroutine

import com.github.shynixn.mccoroutine.entity.MCCoroutineImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import java.lang.reflect.Method

/**
 * Static session.
 */
private val mcCoroutine = MCCoroutineImpl()

/**
 * Launches the given function in the Coroutine Scope of the given plugin.
 * This function may be called immediately without any delay if the Thread
 * calling this function Bukkit.isPrimaryThread() is true. This means
 * for example that event cancelling or modifying return values is still possible.
 * @param f callback function inside a coroutine scope.
 */
fun Plugin.launchMinecraft(f: suspend CoroutineScope.() -> Unit) {

}

/**
 * Launches the given function in the Coroutine Scope of the given plugin async.
 * This function may be called immediately without any delay if the Thread
 * calling this function Bukkit.isPrimaryThread() is false. This means
 * for example that event cancelling or modifying return values is still possible.
 * @param f callback function inside a coroutine scope.
 */
fun Plugin.launchAsync(f: suspend CoroutineScope.() -> Unit) {

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
fun <T : Event> PluginManager.registerSuspendingEventFlow(event: Class<T>, plugin: Plugin): Flow<T> {
    return mcCoroutine.getEventService(plugin).createEventFlow(event)
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
    return mcCoroutine.getEventService(plugin).registerSuspendListener(listener, plugin)
}

/**
 * Internal reflection suspend.
 */
internal suspend fun Method.invokeSuspend(obj: Any, vararg args: Any?): Any? =
    kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn { cont ->
        invoke(obj, *args, cont)
    }

