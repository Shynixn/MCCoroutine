package com.github.shynixn.mccoroutine.folia.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class MainDispatcher(
    private val plugin: Plugin,
    tickRateMs: Long,
) : CoroutineDispatcher(), AutoCloseable {
    private val executor = Executors.newScheduledThreadPool(1) { r ->
        Thread(r, "MCCoroutine-${plugin.name}-MainThread")
    }
    private var actionQueue = ConcurrentLinkedQueue<Runnable>()
    var threadId = -1L

    init {
        executor.submit {
            threadId = Thread.currentThread().id
        }
        executor.scheduleAtFixedRate({
            val actions = ArrayList<Runnable>()
            while (true) {
                val action = actionQueue.poll() ?: break
                actions.add(action)
            }
            for (action in actions) {
                action.run()
            }
        }, 1L, tickRateMs, TimeUnit.MILLISECONDS)
    }

    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!plugin.isEnabled) {
            return
        }

        if (Thread.currentThread().id != threadId) {
            actionQueue.add(block)
        } else {
            block.run()
        }
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * `try`-with-resources statement.
     * However, implementers of this interface are strongly encouraged
     * to make their `close` methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    override fun close() {
        executor.shutdown()
    }
}
