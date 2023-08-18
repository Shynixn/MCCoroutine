package com.github.shynixn.mccoroutine.folia.impl

import com.github.shynixn.mccoroutine.folia.MCCoroutine
import com.github.shynixn.mccoroutine.folia.MCCoroutineConfiguration
import com.github.shynixn.mccoroutine.folia.ShutdownStrategy
import org.bukkit.plugin.Plugin

internal class MCCoroutineConfigurationImpl(private val plugin : Plugin, private val mcCoroutine: MCCoroutine) :
    MCCoroutineConfiguration {
    /**
     * Strategy handling how MCCoroutine is disposed.
     * Defaults to ShutdownStrategy.SCHEDULER.
     */
    override var shutdownStrategy: ShutdownStrategy = ShutdownStrategy.SCHEDULER

    /**
     * Manually disposes the MCCoroutine session for the given plugin.
     */
    override fun disposePluginSession() {
        mcCoroutine.disable(plugin)
    }
}
