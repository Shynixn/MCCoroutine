package com.github.shynixn.mccoroutine.velocity

import com.google.inject.Inject
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger

/**
 * When injecting this class into one instance of your plugin, the instance
 * of your plugin automatically becomes a suspending listener, so you can
 * append suspend to any of your startup methods.
 */
@Suppress("ConvertSecondaryConstructorToPrimary")
class SuspendingPluginContainer {
    /**
     * Gets the proxy server.
     */
    val server: ProxyServer

    /**
     * Gets the logger.
     */
    val logger: Logger

    /**
     * Initializes the MCCoroutine hook into the plugin.
     */
    @Inject
    constructor(pluginContainer: PluginContainer, server: ProxyServer, logger: Logger) {
        this.server = server
        this.logger = logger
        mcCoroutine.setupCoroutineSession(pluginContainer, this)
    }
}
