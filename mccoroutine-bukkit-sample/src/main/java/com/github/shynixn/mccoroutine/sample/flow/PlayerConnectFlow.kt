package com.github.shynixn.mccoroutine.sample.flow

import com.github.shynixn.mccoroutine.asyncDispatcher
import com.github.shynixn.mccoroutine.launchMinecraft
import com.github.shynixn.mccoroutine.registerSuspendingEventFlow
import com.github.shynixn.mccoroutine.sample.impl.UserDataCache
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.withContext
import org.bukkit.Material
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

class PlayerConnectFlow(private val plugin: Plugin, private val userDataCache: UserDataCache) {
    /**
     * Gets called on player join event.
     */
    suspend fun onPlayerJoinEvent(playerJoinEvent: PlayerJoinEvent) {
        val userData = userDataCache.getUserDataFromPlayer(playerJoinEvent.player)
        println("[PlayerConnectFlow] " + playerJoinEvent.player.name + " joined the server. KillCount [${userData.amountOfPlayerKills}].")
    }

    /**
     * Gets called on player quit event.
     */
    suspend fun onPlayerQuitEvent(playerQuitEvent: PlayerQuitEvent) {
        val apple = withContext(plugin.asyncDispatcher) {
            Thread.sleep(500)
            ItemStack(Material.APPLE)
        }

        userDataCache.clearCache(playerQuitEvent.player)
        println("[PlayerConnectFlow] " + playerQuitEvent.player.name + " left the server. Don't forget your " + apple + ".")
    }
}
