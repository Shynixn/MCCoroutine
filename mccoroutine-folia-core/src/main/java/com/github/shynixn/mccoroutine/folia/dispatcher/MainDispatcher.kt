package com.github.shynixn.mccoroutine.folia.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.plugin.Plugin
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class MainDispatcher(
    private val plugin: Plugin,
) : CoroutineDispatcher(), AutoCloseable {
    private val executor = Executors.newFixedThreadPool(1) { r ->
        Thread(r, "MCCoroutine-${plugin.name}-MainThread")
    }
    private var threadId = -1L

    init {
        executor.submit {
            threadId = Thread.currentThread().id
        }
    }

    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!plugin.isEnabled) {
            return
        }

        if (Thread.currentThread().id != threadId) {
            executor.submit(block)
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
