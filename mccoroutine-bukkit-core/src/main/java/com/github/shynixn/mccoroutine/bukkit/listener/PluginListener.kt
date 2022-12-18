package com.github.shynixn.mccoroutine.bukkit.listener

import com.github.shynixn.mccoroutine.bukkit.MCCoroutine
import com.github.shynixn.mccoroutine.bukkit.ShutdownStrategy
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin

internal class PluginListener(private val mcCoroutine: MCCoroutine, private val plugin: Plugin) : Listener {
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
