package com.github.shynixn.mccoroutine.bungeecord.sample.listener

import com.github.shynixn.mccoroutine.bungeecord.sample.impl.UserDataCache
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler

class PlayerConnectListener(private val plugin: Plugin, private val userDataCache: UserDataCache) : Listener {
    /**
     * Gets called on player loggin event.
     */
    @EventHandler
    suspend fun onPlayerJoinEvent(loginEvent: PostLoginEvent) {
        println("[PlayerConnectListener/onPlayerJoinEvent] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
        val userData = userDataCache.getUserDataFromPlayerAsync(loginEvent.player).await()
        println(userData.amountOfEntityKills)
        println("[PlayerConnectListener/onPlayerJoinEvent] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
    }
}
