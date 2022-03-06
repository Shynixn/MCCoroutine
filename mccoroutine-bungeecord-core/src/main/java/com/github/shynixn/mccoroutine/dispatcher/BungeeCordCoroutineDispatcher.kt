package com.github.shynixn.mccoroutine.dispatcher

import com.github.shynixn.mccoroutine.extension.isEnabled
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import net.md_5.bungee.api.plugin.Plugin
import kotlin.coroutines.CoroutineContext

class BungeeCordCoroutineDispatcher(private val plugin: Plugin) : CoroutineDispatcher() {
    /**
     * Returns `true` if the execution of the coroutine should be performed with [dispatch] method.
     * Multithreading in BungeeCord works by 2 different threadPools where
     * it is not clear who scheduled a task. Dispatch task every time.
     */
    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return true
    }

    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!plugin.isEnabled) {
            return
        }

        plugin.proxy.scheduler.runAsync(plugin, block)
    }
}
