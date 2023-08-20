package com.github.shynixn.mccoroutine.folia

/**
 * See https://shynixn.github.io/MCCoroutine/wiki/site/plugindisable for more details.
 */
enum class ShutdownStrategy {
    /**
     * Default shutdown strategy. The coroutine session is
     * disposed automatically on plugin disable along with the BukkitScheduler.
     */
    SCHEDULER,

    /**
     * The coroutine session needs to be explicitly disposed.
     */
    MANUAL
}
