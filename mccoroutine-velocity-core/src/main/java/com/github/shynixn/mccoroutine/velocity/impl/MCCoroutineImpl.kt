package com.github.shynixn.mccoroutine.velocity.impl

import com.github.shynixn.mccoroutine.velocity.CoroutineSession
import com.github.shynixn.mccoroutine.velocity.MCCoroutine
import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.github.shynixn.mccoroutine.velocity.extension.isPluginEnabled
import com.velocitypowered.api.plugin.PluginContainer
import java.util.concurrent.ConcurrentHashMap

class MCCoroutineImpl : MCCoroutine {
    private val items = ConcurrentHashMap<PluginContainer, CoroutineSessionImpl>()

    /**
     * Get coroutine session for the given plugin.
     */
    override fun getCoroutineSession(plugin: PluginContainer): CoroutineSession {
        if (!items.containsKey(plugin)) {
            throw IllegalArgumentException("Inject SuspendingPluginContainer into your plugin class to boot MCCoroutine!")
        }

        return items[plugin]!!
    }

    /**
     * Configures the suspending plugin container with the real plugin Container.
     */
    override fun setupCoroutineSession(plugin: PluginContainer, suspendingPluginContainer: SuspendingPluginContainer) {
        if (items.contains(plugin)) {
            return
        }

        // Velocity does not have any static API functions. Therefore, we need to link plugin and suspending plugin manually.
        if (!plugin.isPluginEnabled(suspendingPluginContainer.server.pluginManager)) {
            throw RuntimeException("Plugin ${plugin.description.name} attempt to start a new coroutine session while being disabled. The dispatcher such as plugin.bungeeCordDispatcher is already disposed at this point and cannot be used!")
        }

        items[plugin] = CoroutineSessionImpl(plugin, suspendingPluginContainer)
    }

    /**
     * Disables coroutine for the given plugin.
     */
    override fun disable(plugin: PluginContainer) {
        if (!items.containsKey(plugin)) {
            return
        }

        val session = items[plugin]!!
        session.dispose()
        items.remove(plugin)
    }
}
