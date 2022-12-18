package com.github.shynixn.mccoroutine.bukkit

/**
 * Additional configurations for MCCoroutine and communication.
 */
interface MCCoroutineConfiguration {
    /**
     * Strategy handling how MCCoroutine is disposed.
     * Defaults to ShutdownStrategy.SCHEDULER.
     *
     * Changing this setting may have an impact on All suspend function you call in
     * onDisable(). Carefully verify your changes.
     */
    var shutdownStrategy: ShutdownStrategy

    /**
     * Manually disposes the MCCoroutine session for the current plugin.
     */
    fun disposePluginSession()
}
