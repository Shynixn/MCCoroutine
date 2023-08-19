package com.github.shynixn.mccoroutine.folia.sample

import com.github.shynixn.mccoroutine.folia.*
import com.github.shynixn.mccoroutine.folia.sample.commandexecutor.AdminCommandExecutor
import com.github.shynixn.mccoroutine.folia.sample.impl.FakeDatabase
import com.github.shynixn.mccoroutine.folia.sample.impl.UserDataCache
import com.github.shynixn.mccoroutine.folia.sample.listener.EntityInteractListener
import com.github.shynixn.mccoroutine.folia.sample.listener.PlayerConnectListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import kotlin.coroutines.CoroutineContext

class MCCoroutineSamplePlugin : SuspendingJavaPlugin() {
    /**
     * Called when this plugin is enabled
     */
    override suspend fun onEnableAsync() {
        println("[MCCoroutineSamplePlugin/onEnableAsync] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")

        withContext(Dispatchers.IO) {
            println("[MCCoroutineSamplePlugin/onEnableAsync] Simulating data load Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            Thread.sleep(500)
        }

        val plugin = this
        val database = FakeDatabase()
        val cache = UserDataCache(this, database)
        val eventDispatcher = mapOf<Class<out Event>, (event: Event) -> CoroutineContext>(
            Pair(PlayerJoinEvent::class.java) {
                require(it is PlayerJoinEvent)
                plugin.entityDispatcher(it.player)
            },
            Pair(PlayerQuitEvent::class.java) {
                require(it is PlayerQuitEvent)
                plugin.entityDispatcher(it.player)
            },
            Pair(MCCoroutineExceptionEvent::class.java) {
                require(it is MCCoroutineExceptionEvent)
                plugin.globalRegionDispatcher
            },
            Pair(EntitySpawnEvent::class.java) {
                require(it is EntitySpawnEvent)
                plugin.entityDispatcher(it.entity)
            },
            Pair(PlayerInteractAtEntityEvent::class.java) {
                require(it is PlayerInteractAtEntityEvent)
                plugin.entityDispatcher(it.player)
            },
        )
        // Extension to traditional registration.
        server.pluginManager.registerSuspendingEvents(
            PlayerConnectListener(this, cache),
            this,
            eventDispatcher
        )
        server.pluginManager.registerSuspendingEvents(
            EntityInteractListener(
                cache
            ), this,
            eventDispatcher
        );

        val commandExecutor = AdminCommandExecutor(cache, this)
        this.getCommand("mccor")!!.setSuspendingExecutor(commandExecutor)
        this.getCommand("mccor")!!.setSuspendingTabCompleter(commandExecutor)

        println("[MCCoroutineSamplePlugin/onEnableAsync] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
        println("[MCCoroutineSamplePlugin/onEnableAsync] Is using Folia Schedulers: " + this.mcCoroutineConfiguration.isFoliaLoaded)
    }


    /**
     * Called when this plugin is disabled.
     */
    override suspend fun onDisableAsync() {
        println("[MCCoroutineSamplePlugin/onDisableAsync] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")

        // Do not use asyncDispatcher as it is already disposed at this point.
        withContext(Dispatchers.IO) {
            println("[MCCoroutineSamplePlugin/onDisableAsync] Simulating data save on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            Thread.sleep(500)
        }

        println("[MCCoroutineSamplePlugin/onDisableAsync] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
    }
}
