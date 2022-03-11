package com.github.shynixn.mccoroutine.dispatcher

import com.github.shynixn.mccoroutine.contract.WakeUpBlockService
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

/**
 * Server Main Thread Dispatcher which is only used during startup of a blocking plugin. Later on, this dispatcher is no longer used.
 */
internal class StartupMinecraftCoroutineDispatcher(
    private val wakeUpBlockService: WakeUpBlockService,
    private val plugin: Plugin
) : MinecraftCoroutineDispatcher(plugin) {
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
