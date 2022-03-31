package com.github.shynixn.mccoroutine.bungeecord

import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.PluginDescription

open class SuspendingPlugin : Plugin {
    /**
     * Default constructor.
     */
    constructor() : super()

    /**
     * Constructor with parameters.
     */
    constructor(proxy: ProxyServer, description: PluginDescription) : super(proxy, description)

    /**
     * Called when this plugin is enabled
     */
    open suspend fun onEnableAsync() {
    }

    /**
     * Called when this plugin is disabled.
     */
    open suspend fun onDisableAsync() {
    }

    /**
     * Called after a plugin is loaded but before it has been enabled.
     *
     *
     * When multiple plugins are loaded, the onLoad() for all plugins is
     * called before any onEnable() is called.
     */
    open suspend fun onLoadAsync() {
    }

    /**
     * Called when this plugin is enabled.
     */
    override fun onEnable() {
        runBlocking {
            onEnableAsync()
        }
    }

    /**
     * Called when this plugin is disabled
     */
    override fun onDisable() {
        runBlocking {
            onDisableAsync()
        }
    }

    /**
     * Called after a plugin is loaded but before it has been enabled.
     *
     *
     * When multiple plugins are loaded, the onLoad() for all plugins is
     * called before any onEnable() is called.
     */
    override fun onLoad() {
        runBlocking {
            onLoadAsync()
        }
    }
}
