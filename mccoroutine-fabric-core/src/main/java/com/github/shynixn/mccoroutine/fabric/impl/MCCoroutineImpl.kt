package com.github.shynixn.mccoroutine.fabric.impl

import com.github.shynixn.mccoroutine.fabric.CoroutineSession
import com.github.shynixn.mccoroutine.fabric.MCCoroutine
import net.fabricmc.api.DedicatedServerModInitializer

class MCCoroutineImpl : MCCoroutine {
    private val items = HashMap<Any, CoroutineSessionImpl>()

    /**
     * Get coroutine session for the given server mod.
     * When using an extension, coroutine scope is bound to the lifetime of the extension.
     */
    override fun getCoroutineSession(extension: DedicatedServerModInitializer): CoroutineSession {
        if (!items.containsKey(extension)) {
            startCoroutineSession(extension)
        }

        return items[extension]!!
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
