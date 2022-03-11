package com.github.shynixn.mccoroutine.contract

interface WakeUpBlockService {
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
