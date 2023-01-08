package com.github.shynixn.mccoroutine.minestom.impl

import com.github.shynixn.mccoroutine.minestom.CoroutineSession
import com.github.shynixn.mccoroutine.minestom.MCCoroutine
import net.minestom.server.MinecraftServer
import net.minestom.server.extensions.Extension

class MCCoroutineImpl : MCCoroutine {
    private val items = HashMap<Any, CoroutineSessionImpl>()

    /**
     * Get coroutine session for the given extension.
     * When using an extension, coroutine scope is bound to the lifetime of the extension.
     */
    override fun getCoroutineSession(extension: Extension): CoroutineSession {
        if (!items.containsKey(extension)) {
            startCoroutineSession(extension)
        }

        return items[extension]!!
    }

    /**
     * Get coroutine session for the given extension.
     * When using an extension, coroutine scope is bound to the lifetime of the entire server.
     */
    override fun getCoroutineSession(minecraftServer: MinecraftServer) : CoroutineSession {
        if (!items.containsKey(minecraftServer)) {
            startCoroutineSession(minecraftServer)
        }

        return items[minecraftServer]!!
    }

    /**
     * Disposes the given extension coroutine session.
     */
    override fun disable(extension: Extension) {
        if (!items.containsKey(extension)) {
            return
        }

        val session = items[extension]!!
        session.dispose()
        items.remove(extension)
    }

    /**
     * Disposes the given server coroutine session.
     */
    override fun disable(minecraftServer: MinecraftServer) {
        if (!items.containsKey(minecraftServer)) {
            return
        }

        val session = items[minecraftServer]!!
        session.dispose()
        items.remove(minecraftServer)
    }

    /**
     * Starts a new coroutine session.
     */
    private fun startCoroutineSession(extension: Any) {
        items[extension] = CoroutineSessionImpl(extension)
    }
}
