package com.github.shynixn.mccoroutine.minestom.sample.extension.listener

import com.github.shynixn.mccoroutine.minestom.MCCoroutineExceptionEvent
import com.github.shynixn.mccoroutine.minestom.sample.extension.impl.UserDataCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.extensions.Extension

class PlayerConnectListener(private val extension: Extension, private val userDataCache: UserDataCache) {
    /**
     * Gets called on player join event.
     */
    suspend fun onPlayerJoinEvent(playerJoinEvent: AsyncPlayerConfigurationEvent) {
        println("[PlayerConnectListener/onPlayerJoinEvent] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
        val userData = userDataCache.getUserDataFromPlayerAsync(playerJoinEvent.player).await()
        println("[PlayerConnectListener/onPlayerJoinEvent] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
    }

    /**
     * Gets called on player quit event.
     */
    suspend fun onPlayerQuitEvent(playerQuitEvent: PlayerDisconnectEvent) {
        println("[PlayerConnectListener/onPlayerQuitEvent] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")

        val apple = withContext(Dispatchers.IO) {
            println("[PlayerConnectListener/onPlayerQuitEvent] Simulate data save on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            Thread.sleep(500)
            "Apple"
        }

        userDataCache.clearCache(playerQuitEvent.player)
        println("[PlayerConnectListener/onPlayerQuitEvent] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
    }

    fun onCoroutineException(event: MCCoroutineExceptionEvent) {
        if (event.extension != extension) {
            // Other extension, we do not care.
            return
        }

        // Print Exception
        event.exception.printStackTrace()
        event.isCancelled = true
    }
}
