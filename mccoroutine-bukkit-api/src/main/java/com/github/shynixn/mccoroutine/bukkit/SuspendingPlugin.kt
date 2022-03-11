package com.github.shynixn.mccoroutine.bukkit

import org.bukkit.plugin.Plugin

/**
 * Extension to the plugin interface for suspendable lifecycle functions.
 */
interface SuspendingPlugin : Plugin {
    /**
     * Called when this plugin is enabled
     */
    suspend fun onEnableAsync();

    /**
     * Called when this plugin is disabled.
     */
    suspend fun onDisableAsync();

    /**
     * Called after a plugin is loaded but before it has been enabled.
     *
     *
     * When multiple plugins are loaded, the onLoad() for all plugins is
     * called before any onEnable() is called.
     */
    suspend fun onLoadAsync();
}
