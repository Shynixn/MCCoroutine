package com.github.shynixn.mccoroutine.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

internal class AsyncCoroutineDispatcher(private val plugin: Plugin) : CoroutineDispatcher() {
    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (plugin.server.isPrimaryThread) {
            plugin.server.scheduler.runTaskAsynchronously(plugin, block)
        } else {
            block.run()
        }
    }
}
