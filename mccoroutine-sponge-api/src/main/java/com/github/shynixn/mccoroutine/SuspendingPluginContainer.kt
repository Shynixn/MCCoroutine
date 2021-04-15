package com.github.shynixn.mccoroutine

import com.google.inject.Inject
import org.spongepowered.api.Sponge
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.Order
import org.spongepowered.api.event.game.state.GameConstructionEvent
import org.spongepowered.api.plugin.PluginContainer

/**
 * When injecting this class into one instance of your plugin, the instance
 * of your plugin automatically becomes a suspending listener, so you can
 * append suspend to any of your startup methods.
 */
class SuspendingPluginContainer {
    @Inject
    private lateinit var internalContainer: PluginContainer

    /**
     * Gets the plugin container.
     */
    val pluginContainer: PluginContainer
        get() {
            return internalContainer
        }

    /**
     * Registers this instance as a listener.
     */
    @Inject
    fun setContainer(pluginContainer: PluginContainer) {
        Sponge.getEventManager().registerListeners(pluginContainer, this)
    }

    /**
     * At the earliest possible moment at the earliest game construction
     * event the plugin instance is swapped with a suspending listener.
     */
    @Listener
    fun onGameInitializeEvent(event: GameConstructionEvent) {
        val instance = internalContainer.instance.get()
        Sponge.getEventManager().unregisterListeners(instance)
        Sponge.getEventManager().registerSuspendingListeners(internalContainer, instance)
    }
}
