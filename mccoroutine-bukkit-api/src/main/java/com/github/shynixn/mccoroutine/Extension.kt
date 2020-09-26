package com.github.shynixn.mccoroutine

import com.github.shynixn.mccoroutine.contract.MCCoroutine
import kotlinx.coroutines.CoroutineScope
import org.bukkit.Bukkit
import org.bukkit.command.PluginCommand
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import java.lang.reflect.Method
import kotlin.coroutines.CoroutineContext

/**
 * Static session.
 */
private val mcCoroutine: MCCoroutine by lazy {
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

/**
 * Gets the server NMS version.
 */
val serverVersion: String by lazy {
    Bukkit.getServer().javaClass.getPackage().name.replace(".", ",").split(",")[3]
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
 * Registers the given packets for event listening.
 */
fun PluginManager.registerPackets(packets: List<Class<*>>, plugin: Plugin) {
    mcCoroutine.getCoroutineSession(plugin).protocolService.registerPackets(packets)
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
 * Sends a native minecraft packet to the player client.
 */
fun <P> Player.sendPacket(plugin: Plugin, packet: P) {
    require(packet is Any)
    return mcCoroutine.getCoroutineSession(plugin).protocolService.sendPacket(this, packet)
}

/**
 * Finds the version compatible class.
 */
fun findClazz(name: String): Class<*> {
    return Class.forName(
        name.replace(
            "VERSION",
            serverVersion
        )
    )
}
