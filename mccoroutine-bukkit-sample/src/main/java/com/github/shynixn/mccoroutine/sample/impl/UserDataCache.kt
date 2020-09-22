package com.github.shynixn.mccoroutine.sample.impl

import com.github.shynixn.mccoroutine.sample.entity.UserData
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.bukkit.entity.Player

class UserDataCache(private val fakeDatabase: FakeDatabase) {
    private val cache = HashMap<Player, Deferred<UserData>>()

    fun clearCache(player: Player) {
        cache.remove(player)
    }

    suspend fun getUserDataFromPlayer(player: Player): UserData {
        return coroutineScope {
            if (!cache.containsKey(player)) {
                cache[player] = async(Dispatchers.IO) {
                    fakeDatabase.getUserDataFromPlayer(player)
                }
            }
            cache[player]!!.await()
        }
    }
}
