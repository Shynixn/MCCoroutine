package com.github.shynixn.mccoroutine.folia.listener

import com.github.shynixn.mccoroutine.folia.MCCoroutine
import com.github.shynixn.mccoroutine.folia.ShutdownStrategy
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin

internal class PluginListener : Listener {
    private val mcCoroutine: MCCoroutine
    private val plugin: Plugin

    constructor(mcCoroutine: MCCoroutine, plugin: Plugin) {
        this.mcCoroutine = mcCoroutine
        this.plugin = plugin
    }

    /**
     * Gets called when the plugin is disabled.
     */
    @EventHandler
    fun onPluginDisable(pluginEvent: PluginDisableEvent) {
        if (pluginEvent.plugin != plugin) {
            return
        }

        val configuration = mcCoroutine.getCoroutineSession(plugin).mcCoroutineConfiguration

        if (configuration.shutdownStrategy == ShutdownStrategy.SCHEDULER) {
            mcCoroutine.disable(pluginEvent.plugin)
        }
    }
}
