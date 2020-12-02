package com.github.shynixn.mccoroutine.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import org.spongepowered.api.Sponge
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.scheduler.Task
import kotlin.coroutines.CoroutineContext

internal class AsyncCoroutineDispatcher(private val plugin: PluginContainer) :
    CoroutineDispatcher() {
    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (Sponge.getServer().isMainThread) {
            Task.builder()
                .async()
                .execute(block)
                .submit(plugin)
        } else {
            block.run()
        }
    }
}
