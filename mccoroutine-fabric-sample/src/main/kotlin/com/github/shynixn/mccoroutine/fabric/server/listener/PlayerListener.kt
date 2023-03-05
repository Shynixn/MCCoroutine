package com.github.shynixn.mccoroutine.fabric.server.listener

import com.github.shynixn.mccoroutine.fabric.server.impl.UserDataCache
import net.fabricmc.api.DedicatedServerModInitializer
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World

class PlayerListener(private val mod: DedicatedServerModInitializer, private val userDataCache: UserDataCache) {
    suspend fun onPlayerAttackEvent(
        player: PlayerEntity,
        world: World,
        hand: Hand,
        entity: Entity,
        hitResult: EntityHitResult?
    ) {
        println("[PlayerConnectListener/onPlayerJoinEvent] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
        val userData = userDataCache.getUserDataFromPlayerAsync(player).await()
        println("[PlayerConnectListener/onPlayerJoinEvent] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
    }

    fun onCoroutineException(throwable: Throwable, entryPoint: Any): Boolean {
        if (entryPoint != mod) {
            return false
        }

        val cancelled = true
        // Print Exception
        throwable.printStackTrace()
        return cancelled
    }
}
