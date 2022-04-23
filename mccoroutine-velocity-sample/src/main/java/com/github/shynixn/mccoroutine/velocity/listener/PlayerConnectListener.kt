package com.github.shynixn.mccoroutine.velocity.listener

import com.github.shynixn.mccoroutine.velocity.impl.UserDataCache
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PostLoginEvent

class PlayerConnectListener(private val userDataCache: UserDataCache) {
    /**
     * Gets called on player login event.
     */
    @Subscribe
    suspend fun onPlayerJoinEvent(loginEvent: PostLoginEvent) {
        println("[PlayerConnectListener/onPlayerJoinEvent] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
        val userData = userDataCache.getUserDataFromPlayerAsync(loginEvent.player).await()
        println(userData.amountOfEntityKills)
        println("[PlayerConnectListener/onPlayerJoinEvent] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
    }
}
