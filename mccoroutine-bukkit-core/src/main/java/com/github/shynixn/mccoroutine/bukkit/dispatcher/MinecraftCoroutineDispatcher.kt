package com.github.shynixn.mccoroutine.bukkit.dispatcher

import com.github.shynixn.mccoroutine.bukkit.CoroutineBukkitTasks
import com.github.shynixn.mccoroutine.bukkit.CoroutineTimings
import com.github.shynixn.mccoroutine.bukkit.service.WakeUpBlockServiceImpl
import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

/**
 * Server Main Thread Dispatcher. Dispatches only if the call is not at the primary thread yet.
 */
internal open class MinecraftCoroutineDispatcher(
    private val plugin: Plugin,
    private val wakeUpBlockService: WakeUpBlockServiceImpl
) : CoroutineDispatcher() {
    /**
     * Returns `true` if the execution of the coroutine should be performed with [dispatch] method.
     * The default behavior for most dispatchers is to return `true`.
     * This method should generally be exception-safe. An exception thrown from this method
     * may leave the coroutines that use this dispatcher in the inconsistent and hard to debug state.
     */
    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        wakeUpBlockService.ensureWakeup()
        return !plugin.server.isPrimaryThread && plugin.isEnabled
    }

    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!plugin.isEnabled) {
            return
        }

        val timedRunnable = context[CoroutineTimings.Key]
        val task = if (timedRunnable == null) {
            plugin.server.scheduler.runTask(plugin, block)
        } else {
            timedRunnable.queue.add(block)
            plugin.server.scheduler.runTask(plugin, timedRunnable)
        }

        val bukkitTaskContext = context[CoroutineBukkitTasks.Key]

        if (bukkitTaskContext != null) {
            bukkitTaskContext.tasks.add(task)
        }
    }
}
