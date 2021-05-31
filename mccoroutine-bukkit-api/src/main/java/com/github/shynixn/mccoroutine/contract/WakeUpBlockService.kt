package com.github.shynixn.mccoroutine.contract

interface WakeUpBlockService {
    /**
     * Enables or disables the server heartbeat hack.
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
