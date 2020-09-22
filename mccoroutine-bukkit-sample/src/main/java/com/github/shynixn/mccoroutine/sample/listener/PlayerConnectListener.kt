package com.github.shynixn.mccoroutine.sample.listener

import com.github.shynixn.mccoroutine.asyncDispatcher
import com.github.shynixn.mccoroutine.registerSuspendingEvents
import com.github.shynixn.mccoroutine.sample.impl.UserDataCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

class PlayerConnectListener(private val plugin: Plugin, private val userDataCache: UserDataCache) : Listener {
    init {
        val pluginManager = plugin.server.pluginManager
        pluginManager.registerSuspendingEvents(this, plugin)
    }

    /**
     * Gets called on player join event.
     */
    @EventHandler
    suspend fun onPlayerJoinEvent(playerJoinEvent: PlayerJoinEvent) {
        val userData = userDataCache.getUserDataFromPlayer(playerJoinEvent.player)
        println("[PlayerConnectListener] " + playerJoinEvent.player.name + " joined the server. KillCount [${userData.amountOfPlayerKills}].")
    }

    /**
     * Gets called on player quit event.
     */
    @EventHandler
    suspend fun onPlayerQuitEvent(playerQuitEvent: PlayerQuitEvent) {
        val apple = withContext(plugin.asyncDispatcher) {
            Thread.sleep(500)
            ItemStack(Material.APPLE)
        }

        println("[PlayerConnectListener] " + playerQuitEvent.player.name + " left the server. Don't forget your " + apple + ".")
    }
}
