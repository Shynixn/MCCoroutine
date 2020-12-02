package com.github.shynixn.mccoroutine.impl

import com.github.shynixn.mccoroutine.contract.CoroutineSession
import com.github.shynixn.mccoroutine.contract.MCCoroutine
import com.github.shynixn.mccoroutine.service.CoroutineSessionImpl
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.plugin.PluginContainer

class MCCoroutineImpl : MCCoroutine {
    private val items = HashMap<PluginContainer, CoroutineSession>()

    /**
     * Get coroutine session for the given plugin.
     */
    override fun getCoroutineSession(plugin: PluginContainer): CoroutineSession {
        if (!items.containsKey(plugin)) {
            startCoroutineSession(plugin)
        }

        return items[plugin]!!
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

    /**
     * Starts a new coroutine session.
     */
    private fun startCoroutineSession(plugin: PluginContainer) {
        val logger = Sponge.getServiceManager().provideUnchecked(Logger::class.java)
        items[plugin] = CoroutineSessionImpl(plugin, logger)
    }
}
