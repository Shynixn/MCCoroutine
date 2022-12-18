package com.github.shynixn.mccoroutine.bukkit.impl

import com.github.shynixn.mccoroutine.bukkit.CoroutineSession
import com.github.shynixn.mccoroutine.bukkit.MCCoroutine
import com.github.shynixn.mccoroutine.bukkit.listener.PluginListener
import org.bukkit.plugin.Plugin

/**
 * A singleton implementation which keeps all coroutine sessions of all plugins.
 */
class MCCoroutineImpl : MCCoroutine {
    private val items = HashMap<Plugin, CoroutineSessionImpl>()

    /**
     * Get coroutine session for the given plugin.
     */
    override fun getCoroutineSession(plugin: Plugin): CoroutineSession {
        if (!items.containsKey(plugin)) {
            startCoroutineSession(plugin)
        }

        return items[plugin]!!
    }

    /**
     * Disables coroutine for the given plugin.
     */
    override fun disable(plugin: Plugin) {
        if (!items.containsKey(plugin)) {
            return
        }

        val session = items[plugin]!!
        session.dispose()
        items.remove(plugin)
    }

    /**
     * Starts a new coroutine session.
     */
    private fun startCoroutineSession(plugin: Plugin) {
        if (!plugin.isEnabled) {
            throw RuntimeException("Plugin ${plugin.name} attempted to start a new coroutine session while being disabled. Dispatchers such as plugin.minecraftDispatcher and plugin.asyncDispatcher are using the BukkitScheduler, which is already disposed at this point of time. If you are starting a coroutine in onDisable, consider using runBlocking or a different plugin.mcCoroutineConfiguration.shutdownStrategy.")
        }

        val pluginListener = PluginListener(this, plugin)
        val coroutineFacade = MCCoroutineConfigurationImpl(plugin, this)
        items[plugin] = CoroutineSessionImpl(plugin, coroutineFacade)
        plugin.server.pluginManager.registerEvents(pluginListener, plugin)
    }
}
