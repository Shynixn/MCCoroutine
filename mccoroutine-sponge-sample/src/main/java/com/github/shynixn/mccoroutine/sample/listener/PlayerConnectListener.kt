package com.github.shynixn.mccoroutine.sample.listener

import com.github.shynixn.mccoroutine.asyncDispatcher
import com.github.shynixn.mccoroutine.sample.commandexecutor.AdminCommandExecutor
import com.github.shynixn.mccoroutine.sample.impl.UserDataCache
import kotlinx.coroutines.withContext
import org.spongepowered.api.Sponge
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.network.ClientConnectionEvent
import org.spongepowered.api.item.ItemTypes
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.plugin.PluginContainer

class PlayerConnectListener(private val plugin: PluginContainer, private val userDataCache: UserDataCache) {
    /**
     * Gets called on player join event.
     */
    @Listener
    suspend fun onPlayerJoinEvent(playerJoinEvent: ClientConnectionEvent.Join) {
        println("[PlayerConnectListener-Join] Is starting on Primary Thread: " + Sponge.getServer().isMainThread)
        val userData = userDataCache.getUserDataFromPlayerAsync(playerJoinEvent.targetEntity).await()
        println("[PlayerConnectListener-Join] " + playerJoinEvent.targetEntity.name + " joined the server. KillCount [${userData.amountOfPlayerKills}].")
        println("[PlayerConnectListener-Join] Is ending on Primary Thread: " + Sponge.getServer().isMainThread)
    }

    /**
     * Gets called on player quit event.
     */
    @Listener
    suspend fun onPlayerQuitEvent(playerQuitEvent: ClientConnectionEvent.Disconnect) {
        println("[PlayerConnectListener-Quit] Is starting on Primary Thread: " + Sponge.getServer().isMainThread)

        val apple = withContext(plugin.asyncDispatcher) {
            Thread.sleep(500)
            ItemStack.builder().itemType(ItemTypes.APPLE).build()
        }

        userDataCache.clearCache(playerQuitEvent.targetEntity)
        println("[PlayerConnectListener-Quit] " + playerQuitEvent.targetEntity.name + " left the server. Don't forget your " + apple + ".")
        println("[PlayerConnectListener-Quit] Is ending on Primary Thread: " + Sponge.getServer().isMainThread)
    }

    /**
     * Gets called on custom event.
     */
    @Listener
    suspend fun onMCCoroutineEvent(event: AdminCommandExecutor.MCCoroutineEvent) {
        println("[PlayerConnectListener-Coroutine] Is starting on Primary Thread: " + Sponge.getServer().isMainThread)

        val apple = withContext(plugin.asyncDispatcher) {
            Thread.sleep(500)
            ItemStack.builder().itemType(ItemTypes.APPLE).build()
        }

        println("[PlayerConnectListener-Coroutine] Don't forget your " + apple + ".")
        println("[PlayerConnectListener-Coroutine] Is ending on Primary Thread: " + Sponge.getServer().isMainThread)
    }
}
