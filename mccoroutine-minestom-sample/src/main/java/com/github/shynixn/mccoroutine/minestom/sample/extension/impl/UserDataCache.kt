package com.github.shynixn.mccoroutine.minestom.sample.extension.impl

import com.github.shynixn.mccoroutine.minestom.sample.extension.entity.UserData
import com.github.shynixn.mccoroutine.minestom.scope
import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import net.minestom.server.entity.Player
import net.minestom.server.extensions.Extension
import java.util.concurrent.CompletionStage

class UserDataCache(private val extension: Extension, private val fakeDatabase: FakeDatabase) {
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
        withContext(Dispatchers.IO) {
            fakeDatabase.saveUserData(userData)
        }
    }

    /**
     * Gets the user data from the player.
     */
    suspend fun getUserDataFromPlayerAsync(player: Player): Deferred<UserData> {
        return coroutineScope {
            println("[UserDataCache/getUserDataFromPlayerAsync] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            if (!cache.containsKey(player)) {
                cache[player] = async(Dispatchers.IO) {
                    println("[UserDataCache/getUserDataFromPlayerAsync] Is downloading data on Thread:${Thread.currentThread().name}/${Thread.currentThread().id})}")
                    fakeDatabase.getUserDataFromPlayer(player)
                }
            }

            cache[player]!!
        }
    }

    /**
     * Gets the user data from the player.
     *
     * This method is only useful if you plan to access suspend functions from Java. It
     * is not possible to call suspend functions directly from java, so we need to
     * wrap it into a Java 8 CompletionStage.
     *
     * This might be useful if you plan to provide a Developer Api for your plugin as other
     * plugins may be written in Java or if you have got Java code in your plugin.
     */
    fun getUserDataFromPlayer(player: Player): CompletionStage<UserData> {
        return extension.scope.future {
            getUserDataFromPlayerAsync(player).await()
        }
    }
}
