package com.github.shynixn.mccoroutine.service

import com.github.shynixn.mccoroutine.contract.WakeUpBlockService
import com.github.shynixn.mccoroutine.findClazz
import org.bukkit.plugin.Plugin
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.locks.LockSupport

class WakeUpBlockServiceImpl(private val plugin: Plugin) : WakeUpBlockService {
    private var threadSupport: ExecutorService? = null
    private val craftSchedulerClazz by lazy {
        plugin.findClazz("org.bukkit.craftbukkit.VERSION.scheduler.CraftScheduler")
    }
    private val craftSchedulerTickField by lazy {
        val field = craftSchedulerClazz.getDeclaredField("currentTick")
        field.isAccessible = true
        field
    }
    private val craftSchedulerHeartBeatMethod by lazy {
        craftSchedulerClazz.getDeclaredMethod("mainThreadHeartbeat", Int::class.java)
    }

    /**
     * Enables or disables the server heartbeat hack.
     */
    override var isManipulatedServerHeartBeatEnabled: Boolean = true

    /**
     * Reference to the primary server thread.
     */
    override var primaryThread: Thread? = null

    /**
     * Calls scheduler management implementations to ensure the
     * is not sleeping if a run is scheduled by blocking.
     */
    override fun ensureWakeup() {
        if (!isManipulatedServerHeartBeatEnabled) {
            if (threadSupport != null) {
                threadSupport!!.shutdown()
                threadSupport = null
            }

            return
        }

        if (primaryThread == null) {
            return
        }

        if (threadSupport == null) {
            threadSupport = Executors.newFixedThreadPool(1)
        }

        threadSupport!!.submit {
            val blockingCoroutine = LockSupport.getBlocker(primaryThread)

            if (blockingCoroutine != null) {
                val currentTick = craftSchedulerTickField.get(plugin.server.scheduler)
                craftSchedulerHeartBeatMethod.invoke(plugin.server.scheduler, currentTick)
            }
        }
    }

    /**
     * Disposes the service.
     */
    override fun dispose() {
        threadSupport?.shutdown()
    }
}
