package com.github.shynixn.mccoroutine.minestom.sample.server.listener

import com.github.shynixn.mccoroutine.minestom.MCCoroutineExceptionEvent
import com.github.shynixn.mccoroutine.minestom.sample.server.impl.UserDataCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerLoginEvent

class PlayerConnectListener(private val server: MinecraftServer, private val userDataCache: UserDataCache) {
    /**
     * Gets called on player join event.
     */
    suspend fun onPlayerJoinEvent(playerJoinEvent: PlayerLoginEvent) {
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
        // Print Exception
        event.exception.printStackTrace()
        event.isCancelled = true
    }
}
