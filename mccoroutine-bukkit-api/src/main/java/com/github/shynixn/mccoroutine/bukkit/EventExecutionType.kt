package com.github.shynixn.mccoroutine.bukkit

/**
 * The mode how suspendable events are executed if dispatched manually.
 */
enum class EventExecutionType {
    Consecutive,
    Concurrent
}
