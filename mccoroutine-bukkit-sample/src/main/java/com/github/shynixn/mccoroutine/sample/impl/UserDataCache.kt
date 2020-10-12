package com.github.shynixn.mccoroutine.sample.impl

import com.github.shynixn.mccoroutine.asyncDispatcher
import com.github.shynixn.mccoroutine.sample.entity.UserData
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class UserDataCache(private val plugin: Plugin, private val fakeDatabase: FakeDatabase) {
    private val cache = HashMap<Player, Deferred<UserData>>()

    /**
     * Clears the player cache.
     */
    fun clearCache(player: Player) {
        cache.remove(player)
    }

    /**
     * Saves the cached data of the player.
     */
    suspend fun saveUserData(player: Player) {
        val userData = cache[player]!!.await()
        withContext(plugin.asyncDispatcher) {
            fakeDatabase.saveUserData(userData)
        }
    }

    /**
     * Gets the user data from the player.
     */
    suspend fun getUserDataFromPlayer(player: Player): UserData {
        return coroutineScope {
            if (!cache.containsKey(player)) {
                cache[player] = async(plugin.asyncDispatcher) {
                    println("[Cache] is downloading async: " + !Bukkit.isPrimaryThread())
                    fakeDatabase.getUserDataFromPlayer(player)
                }
            }
            println("[Cache] is downloading waiting on Primary Thread: " + Bukkit.isPrimaryThread())
            cache[player]!!.await()
        }
    }
}
