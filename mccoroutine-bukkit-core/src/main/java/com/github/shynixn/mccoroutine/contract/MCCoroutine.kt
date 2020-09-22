package com.github.shynixn.mccoroutine.contract

import org.bukkit.plugin.Plugin

internal interface MCCoroutine {
    /**
     * Get coroutine session for the given plugin.
     */
    fun getCoroutineSession(plugin: Plugin): CoroutineSession

    /**
     * Disables coroutine for the given plugin.
     */
    fun disable(plugin: Plugin)
}
