package com.github.shynixn.mccoroutine.bukkit.test.impl

import com.github.shynixn.mccoroutine.bukkit.MCCoroutine
import com.github.shynixn.mccoroutine.bukkit.MCCoroutineConfiguration
import com.github.shynixn.mccoroutine.bukkit.ShutdownStrategy
import org.bukkit.plugin.Plugin

internal class TestMCCoroutineConfigurationImpl(private val plugin: Plugin, private val mcCoroutine: MCCoroutine) :
    MCCoroutineConfiguration {
    override var shutdownStrategy: ShutdownStrategy = ShutdownStrategy.SCHEDULER

    override fun disposePluginSession() {
        mcCoroutine.disable(plugin)
    }
}
