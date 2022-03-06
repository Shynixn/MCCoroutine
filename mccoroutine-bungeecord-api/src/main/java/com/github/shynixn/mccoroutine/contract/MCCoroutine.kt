package com.github.shynixn.mccoroutine.contract

import net.md_5.bungee.api.plugin.Plugin

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
