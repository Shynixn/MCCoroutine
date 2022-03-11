package com.github.shynixn.mccoroutine.bukkit.internal

import org.bukkit.plugin.Plugin

interface MCCoroutine {
    /**
     * Get coroutine session for the given plugin.
     */
    fun getCoroutineSession(plugin: Plugin): CoroutineSession

    /**
     * Disables coroutine for the given plugin.
     */
    fun disable(plugin: Plugin)
}
