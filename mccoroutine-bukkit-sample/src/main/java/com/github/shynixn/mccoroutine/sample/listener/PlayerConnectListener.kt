package com.github.shynixn.mccoroutine.sample.listener

import com.github.shynixn.mccoroutine.PlayerPacketEvent
import com.github.shynixn.mccoroutine.asyncDispatcher
import com.github.shynixn.mccoroutine.sample.impl.UserDataCache
import com.github.shynixn.mccoroutine.sample.packet.MyPacketPlayInPositionLooks
import com.github.shynixn.mccoroutine.sample.packet.MySupportedPacketType
import kotlinx.coroutines.withContext
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

        userDataCache.clearCache(playerQuitEvent.player)
        println("[PlayerConnectListener] " + playerQuitEvent.player.name + " left the server. Don't forget your " + apple + ".")
    }

    /**
     * Gets called on player packet event.
     */
    @EventHandler
    suspend fun onPlayerPacketEvent(playerPacketEvent: PlayerPacketEvent) {
        val packetType = MySupportedPacketType.findSupportedPacketType(playerPacketEvent.packet.javaClass.simpleName)

        if (packetType != MySupportedPacketType.PACKETPLAYINPOSITIONLOOK) {
            return
        }

        val myPacket = MyPacketPlayInPositionLooks(playerPacketEvent.byteData)

        val apple = withContext(plugin.asyncDispatcher) {
            Thread.sleep(500)
            ItemStack(Material.APPLE)
        }

        println("Player: " + playerPacketEvent.player + " received packet: " + myPacket + ".  Don't forget your " + apple + ".")
    }
}
