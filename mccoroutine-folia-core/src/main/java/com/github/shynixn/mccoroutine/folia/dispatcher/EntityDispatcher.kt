package com.github.shynixn.mccoroutine.folia.dispatcher

import com.github.shynixn.mccoroutine.folia.regionDispatcher
import com.github.shynixn.mccoroutine.folia.service.WakeUpBlockServiceImpl
import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

internal open class EntityDispatcher(
    private val plugin: Plugin,
    private val wakeUpBlockService: WakeUpBlockServiceImpl,
    private val entity: Entity
) : CoroutineDispatcher() {
    /**
     * Returns `true` if the execution of the coroutine should be performed with [dispatch] method.
     * The default behavior for most dispatchers is to return `true`.
     * This method should generally be exception-safe. An exception thrown from this method
     * may leave the coroutines that use this dispatcher in the inconsistent and hard to debug state.
     */
    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        wakeUpBlockService.ensureWakeup()
        return true
    }

    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        val task = entity.scheduler.run(plugin, {
            block.run()
        }, {
            block.run()
        })

        if (task == null) { // Entity was removed. Try to detect region
            plugin.server.globalRegionScheduler.execute(plugin, block)
        }
    }
}
