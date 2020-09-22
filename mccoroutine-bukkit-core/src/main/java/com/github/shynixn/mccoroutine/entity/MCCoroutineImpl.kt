package com.github.shynixn.mccoroutine.entity

import com.github.shynixn.mccoroutine.contract.CoroutineSession
import com.github.shynixn.mccoroutine.contract.EventService
import com.github.shynixn.mccoroutine.contract.MCCoroutine
import com.github.shynixn.mccoroutine.listener.PluginListener
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

internal class MCCoroutineImpl : MCCoroutine {
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
     * Gets the event service for the given plugin.
     */
    override fun getEventService(plugin: Plugin): EventService {
        TODO("Not yet implemented")
    }

    /**
     * Disables coroutine for the given plugin.
     */
    override fun disable(plugin: Plugin) {
        TODO("Not yet implemented")
    }

    /**
     * Starts a new coroutine session.
     */
    private fun startCoroutineSession(plugin: Plugin) {
        val pluginListener = PluginListener(this, plugin)
        val coroutineSession = CoroutineSessionImpl()
        items[plugin] = coroutineSession
        plugin.server.pluginManager.registerEvents(pluginListener, plugin)
    }
}
