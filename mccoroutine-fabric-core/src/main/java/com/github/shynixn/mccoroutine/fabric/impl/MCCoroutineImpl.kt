package com.github.shynixn.mccoroutine.fabric.impl

import com.github.shynixn.mccoroutine.fabric.CoroutineSession
import com.github.shynixn.mccoroutine.fabric.MCCoroutine

class MCCoroutineImpl : MCCoroutine {
    private val items = HashMap<Any, CoroutineSessionImpl>()

    /**
     * Get coroutine session for the given mod.
     * When using an extension, coroutine scope is bound to the lifetime of the extension.
     */
    override fun getCoroutineSession(handler: Any): CoroutineSession {
        if (!items.containsKey(handler)) {
            startCoroutineSession(handler)
        }

        return items[handler]!!
    }

    /**
     * Disposes the given coroutine session.
     */
    override fun disable(handler: Any) {
        if (!items.containsKey(handler)) {
            return
        }

        val session = items[handler]!!
        session.dispose()
        items.remove(handler)
    }

    /**
     * Starts a new coroutine session.
     */
    private fun startCoroutineSession(extension: Any) {
        val mcCoroutineConfiguration = MCCoroutineConfigurationImpl(extension, this)
        items[extension] = CoroutineSessionImpl(extension, mcCoroutineConfiguration)
    }
}
