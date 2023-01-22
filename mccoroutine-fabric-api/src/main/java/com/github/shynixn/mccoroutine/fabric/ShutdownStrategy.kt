package com.github.shynixn.mccoroutine.fabric

/**
 * See https://shynixn.github.io/MCCoroutine/wiki/site/plugindisable for more details.
 */
enum class ShutdownStrategy {
    /**
     * Default shutdown strategy.
     * The coroutine session needs to be explicitly disposed.
     */
    MANUAL
}
