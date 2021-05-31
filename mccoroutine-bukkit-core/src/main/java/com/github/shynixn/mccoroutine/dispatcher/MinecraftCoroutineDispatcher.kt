package com.github.shynixn.mccoroutine.dispatcher

import com.github.shynixn.mccoroutine.contract.WakeUpBlockService
import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

internal class MinecraftCoroutineDispatcher(
    private val plugin: Plugin,
    private val wakeUpBlockService: WakeUpBlockService
) : CoroutineDispatcher() {
    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!plugin.isEnabled) {
            return
        }

        if (wakeUpBlockService.primaryThread == null && plugin.server.isPrimaryThread) {
            wakeUpBlockService.primaryThread = Thread.currentThread()
        }

        if (plugin.server.isPrimaryThread) {
            block.run()
        } else {
            plugin.server.scheduler.runTask(plugin, block)
            wakeUpBlockService.ensureWakeup()
        }
    }
}
