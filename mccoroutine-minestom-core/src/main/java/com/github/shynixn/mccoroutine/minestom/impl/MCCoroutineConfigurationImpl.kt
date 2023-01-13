package com.github.shynixn.mccoroutine.minestom.impl

import com.github.shynixn.mccoroutine.minestom.MCCoroutine
import com.github.shynixn.mccoroutine.minestom.MCCoroutineConfiguration
import com.github.shynixn.mccoroutine.minestom.ShutdownStrategy
import net.minestom.server.MinecraftServer
import net.minestom.server.extensions.Extension

internal class MCCoroutineConfigurationImpl(private val extension: Any, private val mcCoroutine: MCCoroutine) :
    MCCoroutineConfiguration {
    /**
     * Strategy handling how MCCoroutine is disposed.
     * Defaults to ShutdownStrategy.MANUAL.
     */
    override var shutdownStrategy: ShutdownStrategy = ShutdownStrategy.MANUAL

    /**
     * Manually disposes the MCCoroutine session for the given plugin.
     */
    override fun disposePluginSession() {
        if (extension is MinecraftServer) {
            mcCoroutine.disable(extension)
        } else if (extension is Extension) {
            mcCoroutine.disable(extension)
        }
    }
}
