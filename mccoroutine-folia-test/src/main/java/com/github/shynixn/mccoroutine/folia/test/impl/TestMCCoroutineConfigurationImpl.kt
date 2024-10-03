package com.github.shynixn.mccoroutine.folia.test.impl

import com.github.shynixn.mccoroutine.folia.MCCoroutine
import com.github.shynixn.mccoroutine.folia.MCCoroutineConfiguration
import com.github.shynixn.mccoroutine.folia.ShutdownStrategy
import org.bukkit.plugin.Plugin

internal class TestMCCoroutineConfigurationImpl(private val plugin: Plugin, private val mcCoroutine: MCCoroutine) :
    MCCoroutineConfiguration {
    override var shutdownStrategy: ShutdownStrategy = ShutdownStrategy.SCHEDULER

    /**
     * Gets if the Folia schedulers where successfully loaded into MCCoroutine.
     * Returns false if MCCoroutine falls back to the BukkitScheduler.
     */
    override val isFoliaLoaded: Boolean
        get() = true

    override fun disposePluginSession() {
        mcCoroutine.disable(plugin)
    }
}
