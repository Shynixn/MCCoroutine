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
        println("[PlayerConnectListener-Join] Is starting on Primary Thread: " + Bukkit.isPrimaryThread())
        val userData = userDataCache.getUserDataFromPlayerAsync(playerJoinEvent.player).await()
        println("[PlayerConnectListener-Join] " + playerJoinEvent.player.name + " joined the server. KillCount [${userData.amountOfPlayerKills}].")
        println("[PlayerConnectListener-Join] Is ending on Primary Thread: " + Bukkit.isPrimaryThread())
    }

    /**
     * Gets called on player quit event.
     */
    @EventHandler
    suspend fun onPlayerQuitEvent(playerQuitEvent: PlayerQuitEvent) {
        println("[PlayerConnectListener-Quit] Is starting on Primary Thread: " + Bukkit.isPrimaryThread())

        val apple = withContext(plugin.asyncDispatcher) {
            Thread.sleep(500)
            ItemStack(Material.APPLE)
        }

        userDataCache.clearCache(playerQuitEvent.player)
        println("[PlayerConnectListener-Quit] " + playerQuitEvent.player.name + " left the server. Don't forget your " + apple + ".")
        println("[PlayerConnectListener-Quit] Is ending on Primary Thread: " + Bukkit.isPrimaryThread())
    }

    @EventHandler
    fun onCoroutineException(event: MCCoroutineExceptionEvent) {
        if (event.plugin != plugin) {
            // Other plugin, we do not care.
            return
        }

        // Print Exception
        event.exception.printStackTrace()
    }
}
