package com.github.shynixn.mccoroutine.folia.sample.listener

import com.github.shynixn.mccoroutine.folia.*
import com.github.shynixn.mccoroutine.folia.sample.impl.UserDataCache
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntitySpawnEvent
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
        println("[PlayerConnectListener/onPlayerJoinEvent] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
        val userData = userDataCache.getUserDataFromPlayerAsync(playerJoinEvent.player).await()
        println("[PlayerConnectListener/onPlayerJoinEvent] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
    }

    /**
     * Gets called on player quit event.
     */
    @EventHandler
    suspend fun onPlayerQuitEvent(playerQuitEvent: PlayerQuitEvent) {
        println("[PlayerConnectListener/onPlayerQuitEvent] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")

        val apple = withContext(plugin.asyncDispatcher) {
            println("[PlayerConnectListener/onPlayerQuitEvent] Simulate data save on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            Thread.sleep(500)
            ItemStack(Material.APPLE)
        }

        withContext(plugin.mainDispatcher) {
            userDataCache.clearCache(playerQuitEvent.player)
        }
        println("[PlayerConnectListener/onPlayerQuitEvent] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
    }

    @EventHandler
    fun onEntitySpawnEvent(event: EntitySpawnEvent) {
        println("[PlayerConnectListener/onEntitySpawnEvent] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
        plugin.launch {
            println("[PlayerConnectListener/onEntitySpawnEvent] Entering coroutine on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            delay(2000)

            val entityLocation = withContext(plugin.entityDispatcher(event.entity)) {
                println("[PlayerConnectListener/onEntitySpawnEvent] Entity Dispatcher on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
                event.entity.customName = "Coroutine Entity"
                event.entity.location
            }

            entityLocation.add(2.0, 0.0, 0.0)
            delay(1000)

            withContext(plugin.regionDispatcher(entityLocation)) {
                println("[PlayerConnectListener/onEntitySpawnEvent] Region Dispatcher on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
                entityLocation.block.type = Material.GOLD_BLOCK
            }

            delay(1000)

            withContext(plugin.entityDispatcher(event.entity)) {
                println("[PlayerConnectListener/onEntitySpawnEvent] Entity Dispatcher on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
                event.entity.teleportAsync(entityLocation)
            }
        }
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
