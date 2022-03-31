package com.github.shynixn.mccoroutine.bungeecord.impl

import com.github.shynixn.mccoroutine.bungeecord.CoroutineSession
import com.github.shynixn.mccoroutine.bungeecord.MCCoroutine
import com.github.shynixn.mccoroutine.bungeecord.extension.isEnabled
import net.md_5.bungee.api.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap

class MCCoroutineImpl : MCCoroutine {
    private val items = ConcurrentHashMap<Plugin, CoroutineSessionImpl>()

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
            throw RuntimeException("Plugin ${plugin.description.name} attempt to start a new coroutine session while being disabled. The dispatcher such as plugin.bungeeCordDispatcher is already disposed at this point and cannot be used!")
        }

        items[plugin] = CoroutineSessionImpl(plugin)
    }
}
