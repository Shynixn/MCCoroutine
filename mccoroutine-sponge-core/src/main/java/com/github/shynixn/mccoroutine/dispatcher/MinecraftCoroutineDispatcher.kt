package com.github.shynixn.mccoroutine.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.scheduler.Task
import kotlin.coroutines.CoroutineContext

internal class MinecraftCoroutineDispatcher(private val plugin: PluginContainer, private val primaryThreadId: Long) :
    CoroutineDispatcher() {
    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (Thread.currentThread().id == primaryThreadId) {
            block.run()
        } else {
            Task.builder()
                .execute(block)
                .submit(plugin)
        }
    }
}
