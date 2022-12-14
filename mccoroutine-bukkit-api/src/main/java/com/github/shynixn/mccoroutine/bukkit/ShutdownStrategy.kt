package com.github.shynixn.mccoroutine.bukkit

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
