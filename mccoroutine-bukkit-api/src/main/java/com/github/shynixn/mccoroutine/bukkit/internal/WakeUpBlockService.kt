package com.github.shynixn.mccoroutine.bukkit.internal

interface WakeUpBlockService {
    /**
     * Enables or disables the server heartbeat hack.
     * This is only active during plugin.onEnable() and does not harm the server.
     * Thread parking and unParking is a complex topic.
     */
    var isManipulatedServerHeartBeatEnabled: Boolean

    /**
     * Reference to the primary server thread.
     */
    var primaryThread: Thread?

    /**
     * Calls scheduler management implementations to ensure the
     * is not sleeping if a run is scheduled by blocking.
     */
    fun ensureWakeup()

    /**
     * Disposes the service.
     */
    fun dispose()
}
