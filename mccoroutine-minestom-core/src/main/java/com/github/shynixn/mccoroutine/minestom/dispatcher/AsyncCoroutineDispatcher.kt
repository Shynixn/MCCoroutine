package com.github.shynixn.mccoroutine.minestom.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import net.minestom.server.MinecraftServer
import net.minestom.server.timer.ExecutionType
import kotlin.coroutines.CoroutineContext

/**
 * Minestom Async ThreadPool Dispatcher. Always dispatch.
 */
internal open class AsyncCoroutineDispatcher() : CoroutineDispatcher() {
    /**
     * Returns `true` if the execution of the coroutine should be performed with [dispatch] method.
     * The default behavior for most dispatchers is to return `true`.
     * This method should generally be exception-safe. An exception thrown from this method
     * may leave the coroutines that use this dispatcher in the inconsistent and hard to debug state.
     */
    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return true
    }

    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        MinecraftServer.getSchedulerManager().scheduleNextTick(block, ExecutionType.TICK_START)
    }
}
