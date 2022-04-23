package com.github.shynixn.mccoroutine.velocity.dispatcher

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.github.shynixn.mccoroutine.velocity.extension.isPluginEnabled
import com.velocitypowered.api.plugin.PluginContainer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

internal class VelocityCoroutineDispatcher(
    private val pluginContainer: PluginContainer,
    private val suspendingPluginContainer: SuspendingPluginContainer
) : CoroutineDispatcher() {
    /**
     * Returns `true` if the execution of the coroutine should be performed with [dispatch] method.
     * Multithreading in Velocity works by different threadPools where
     * it is not clear who scheduled a task. Dispatch task every time.
     */
    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return true
    }

    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!pluginContainer.isPluginEnabled(suspendingPluginContainer.server.pluginManager)) {
            return
        }

        suspendingPluginContainer.server.scheduler
            .buildTask(pluginContainer, block)
            .schedule()
    }
}
