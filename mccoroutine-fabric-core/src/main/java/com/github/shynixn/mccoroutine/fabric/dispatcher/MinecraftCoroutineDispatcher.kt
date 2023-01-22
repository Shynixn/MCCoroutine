package com.github.shynixn.mccoroutine.fabric.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import java.util.concurrent.Executor
import kotlin.coroutines.CoroutineContext

/**
 * Main Thread Dispatcher. Dispatches only if the call is not at the primary thread yet.
 */
internal open class MinecraftCoroutineDispatcher(private val executor: Executor) : CoroutineDispatcher() {
    private var mainThreadId: Long = -1

    init {
        executor.execute {
            mainThreadId = Thread.currentThread().id
        }
    }

    /**
     * Returns `true` if the execution of the coroutine should be performed with [dispatch] method.
     * The default behavior for most dispatchers is to return `true`.
     * This method should generally be exception-safe. An exception thrown from this method
     * may leave the coroutines that use this dispatcher in the inconsistent and hard to debug state.
     */
    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return Thread.currentThread().id != mainThreadId
    }

    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        executor.execute(block)
    }
}
