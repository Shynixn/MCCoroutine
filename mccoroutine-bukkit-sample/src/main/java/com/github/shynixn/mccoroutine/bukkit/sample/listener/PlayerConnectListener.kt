package com.github.shynixn.mccoroutine.bukkit.sample.listener

import com.github.shynixn.mccoroutine.bukkit.MCCoroutineExceptionEvent
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.sample.impl.UserDataCache
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

class PlayerConnectListener(private val plugin: Plugin, private val userDataCache: UserDataCache) : Listener {
    /**
     * Gets called on player join event.
     */
    @EventHandler
    suspend fun onPlayerJoinEvent(playerJoinEvent: PlayerJoinEvent) {
        println("[PlayerConnectListener/onPlayerJoinEvent] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")
        val userData = userDataCache.getUserDataFromPlayerAsync(playerJoinEvent.player).await()
        println("[PlayerConnectListener/onPlayerJoinEvent] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")
    }

    /**
     * Gets called on player quit event.
     */
    @EventHandler
    suspend fun onPlayerQuitEvent(playerQuitEvent: PlayerQuitEvent) {
        println("[PlayerConnectListener/onPlayerQuitEvent] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")

        val apple = withContext(plugin.asyncDispatcher) {
            println("[PlayerConnectListener/onPlayerQuitEvent] Simulate data save on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")
            Thread.sleep(500)
            ItemStack(Material.APPLE)
        }

        userDataCache.clearCache(playerQuitEvent.player)
        println("[PlayerConnectListener/onPlayerQuitEvent] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")
    }

    @EventHandler
    fun onCoroutineException(event: MCCoroutineExceptionEvent) {
        if (event.plugin != plugin) {
            // Other plugin, we do not care.
            return
        }

        // Print Exception
        event.exception.printStackTrace()
        event.isCancelled = true
    }
}
