package com.github.shynixn.mccoroutine.folia.impl

import com.github.shynixn.mccoroutine.folia.MCCoroutine
import com.github.shynixn.mccoroutine.folia.MCCoroutineConfiguration
import com.github.shynixn.mccoroutine.folia.ShutdownStrategy
import org.bukkit.plugin.Plugin

internal class MCCoroutineConfigurationImpl(private val plugin: Plugin, private val mcCoroutine: MCCoroutine) :
    MCCoroutineConfiguration {
    /**
     * Strategy handling how MCCoroutine is disposed.
     * Defaults to ShutdownStrategy.SCHEDULER.
     */
    override var shutdownStrategy: ShutdownStrategy = ShutdownStrategy.SCHEDULER

    /**
     * How often the main plugin dispatcher scheduler ticks.
     * Defaults to 16ms ~ 60 times per second
     */
    override var mainDispatcherTickRateMs: Long = 16

    /**
     * Gets if the Folia schedulers where successfully loaded into MCCoroutine.
     * Returns false if MCCoroutine falls back to the BukkitScheduler.
     */
    override val isFoliaLoaded: Boolean
        get() {
            return mcCoroutine.getCoroutineSession(plugin).isFoliaLoaded
        }

    /**
     * Manually disposes the MCCoroutine session for the given plugin.
     */
    override fun disposePluginSession() {
        mcCoroutine.disable(plugin)
    }
}
