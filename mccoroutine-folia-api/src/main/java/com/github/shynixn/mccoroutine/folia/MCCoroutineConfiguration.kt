package com.github.shynixn.mccoroutine.folia

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
     * How often the main plugin dispatcher scheduler ticks.
     * Defaults to 16ms ~ 60 times per second
     */
    var mainDispatcherTickRateMs : Long

    /**
     * Gets if the Folia schedulers where successfully loaded into MCCoroutine.
     * Returns false if MCCoroutine falls back to the BukkitScheduler.
     */
    val isFoliaLoaded: Boolean

    /**
     * Manually disposes the MCCoroutine session for the current plugin.
     */
    fun disposePluginSession()
}
