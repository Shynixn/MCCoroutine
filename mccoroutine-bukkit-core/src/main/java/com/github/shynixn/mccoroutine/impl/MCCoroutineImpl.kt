package com.github.shynixn.mccoroutine.impl

import com.github.shynixn.mccoroutine.contract.CoroutineSession
import com.github.shynixn.mccoroutine.contract.MCCoroutine
import com.github.shynixn.mccoroutine.service.CoroutineSessionImpl
import com.github.shynixn.mccoroutine.listener.PluginListener
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

class MCCoroutineImpl : MCCoroutine {
    private val items = HashMap<Plugin, CoroutineSession>()

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
            throw RuntimeException("Plugin ${plugin.name} attempt to start a new coroutine session while being disabled. If you need to call a suspension method in JavaPlugin\$onDisable, use kotlinx.coroutines.runblocking{} instead of launch{}.")
        }

        val pluginListener = PluginListener(this, plugin)
        items[plugin] = CoroutineSessionImpl(plugin)

        for (player in plugin.server.onlinePlayers) {
            getCoroutineSession(plugin).protocolService.register(player)
        }

        plugin.server.pluginManager.registerEvents(pluginListener, plugin)
    }
}
