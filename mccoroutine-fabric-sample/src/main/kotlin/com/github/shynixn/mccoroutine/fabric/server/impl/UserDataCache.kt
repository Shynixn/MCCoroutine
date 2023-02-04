package com.github.shynixn.mccoroutine.fabric.server.impl

import com.github.shynixn.mccoroutine.fabric.scope
import com.github.shynixn.mccoroutine.fabric.server.entity.UserData
import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import net.fabricmc.api.DedicatedServerModInitializer
import net.minecraft.entity.player.PlayerEntity
import java.util.concurrent.CompletionStage

class UserDataCache(private val mod: DedicatedServerModInitializer, private val fakeDatabase: FakeDatabase) {
    private val cache = HashMap<PlayerEntity, Deferred<UserData>>()

    /**
     * Clears the player cache.
     */
    fun clearCache(player: PlayerEntity) {
        cache.remove(player)
    }

    /**
     * Saves the cached data of the player.
     */
    suspend fun saveUserData(player: PlayerEntity) {
        val userData = cache[player]!!.await()
        withContext(Dispatchers.IO) {
            fakeDatabase.saveUserData(userData)
        }
    }

    /**
     * Gets the user data from the player.
     */
    suspend fun getUserDataFromPlayerAsync(player: PlayerEntity): Deferred<UserData> {
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
    fun getUserDataFromPlayer(player: PlayerEntity): CompletionStage<UserData> {
        return mod.scope.future {
            getUserDataFromPlayerAsync(player).await()
        }
    }
}
