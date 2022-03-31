package com.github.shynixn.mccoroutine.sample.impl

import com.github.shynixn.mccoroutine.bungeecord.scope
import com.github.shynixn.mccoroutine.sample.entity.UserData
import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin
import java.util.concurrent.CompletionStage
import java.util.concurrent.ConcurrentHashMap

class UserDataCache(private val plugin: Plugin, private val fakeDatabase: FakeDatabase) {
    // ConcurrentHashmap is important because bungeeCord entirely works on multi threading.
    private val cache = ConcurrentHashMap<ProxiedPlayer, Deferred<UserData>>()

    /**
     * Clears the player cache.
     */
    fun clearCache(player: ProxiedPlayer) {
        cache.remove(player)
    }

    /**
     * Saves the cached data of the player.
     */
    suspend fun saveUserData(player: ProxiedPlayer) {
        val userData = cache[player]!!.await()
        println("[UserDataCache/saveUserData] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
        withContext(Dispatchers.IO) {
            fakeDatabase.saveUserData(userData)
            println("[UserDataCache/saveUserData] Is saving on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
        }
        println("[UserDataCache/saveUserData] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
    }

    /**
     * Gets the user data from the player.
     */
    suspend fun getUserDataFromPlayerAsync(player: ProxiedPlayer): Deferred<UserData> {
        return coroutineScope {
            if (!cache.containsKey(player)) {
                println("[UserDataCache/getUserDataFromPlayerAsync] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
                cache[player] = async(Dispatchers.IO) {
                    println("[UserDataCache/getUserDataFromPlayerAsync] Is downloading on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
                    fakeDatabase.getUserDataFromPlayer(player)
                }
            }

            println("[UserDataCache/getUserDataFromPlayerAsync] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
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
    fun getUserDataFromPlayer(player: ProxiedPlayer): CompletionStage<UserData> {
        return plugin.scope.future {
            getUserDataFromPlayerAsync(player).await()
        }
    }
}
