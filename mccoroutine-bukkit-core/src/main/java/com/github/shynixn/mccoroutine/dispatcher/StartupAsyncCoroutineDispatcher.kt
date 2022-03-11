package com.github.shynixn.mccoroutine.dispatcher

import com.github.shynixn.mccoroutine.contract.WakeUpBlockService
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

/**
 * CraftBukkit Async ThreadPool Dispatcher which is only used during startup of a blocking plugin. Later on, this dispatcher is no longer used.
 */
internal class StartupAsyncCoroutineDispatcher(
    private val wakeUpBlockService: WakeUpBlockService,
    private val plugin: Plugin
) : AsyncCoroutineDispatcher(plugin) {
    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!plugin.isEnabled) {
            return
        }

        super.dispatch(context, block)
        wakeUpBlockService.ensureWakeup()
    }
}
