package com.github.shynixn.mccoroutine.bukkit.service

import com.github.shynixn.mccoroutine.bukkit.extension.findClazz
import org.bukkit.plugin.Plugin
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.locks.LockSupport

/**
 * This implementation is only active during plugin startup. Does not affect the server when running.
 */
internal class WakeUpBlockServiceImpl(private val plugin: Plugin) {
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
    var isManipulatedServerHeartBeatEnabled: Boolean = false

    /**
     * Reference to the primary server thread.
     */
    var primaryThread: Thread? = null

    /**
     * Calls scheduler management implementations to ensure the
     * is not sleeping if a run is scheduled by blocking.
     */
    fun ensureWakeup() {
        if (!isManipulatedServerHeartBeatEnabled) {
            if (threadSupport != null) {
                threadSupport!!.shutdown()
                threadSupport = null
            }

            // In all cases except startup, the call immediately returns here.
            return
        }

        if (primaryThread == null && plugin.server.isPrimaryThread) {
            primaryThread = Thread.currentThread()
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
    fun dispose() {
        threadSupport?.shutdown()
    }
}
