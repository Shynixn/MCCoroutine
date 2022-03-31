package com.github.shynixn.mccoroutine.sponge

/**
 * The mode how suspendable events are executed if dispatched manually.
 */
enum class EventExecutionType {
    Consecutive,
    Concurrent
}
