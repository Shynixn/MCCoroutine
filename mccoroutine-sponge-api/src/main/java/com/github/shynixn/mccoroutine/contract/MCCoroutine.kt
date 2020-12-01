package com.github.shynixn.mccoroutine.contract

import org.spongepowered.api.plugin.PluginContainer

interface MCCoroutine {
    /**
     * Get coroutine session for the given plugin.
     */
    fun getCoroutineSession(plugin: PluginContainer): CoroutineSession

    /**
     * Gets the command service.
     */
    val commandService: CommandService

    /**
     * Primary Thread id.
     */
    var primaryThreadId: Long

    /**
     * Disables coroutine for the given plugin.
     */
    fun disable(plugin: PluginContainer)
}
