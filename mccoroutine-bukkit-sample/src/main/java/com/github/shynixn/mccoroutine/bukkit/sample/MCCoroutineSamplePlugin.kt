package com.github.shynixn.mccoroutine.bukkit.sample

import com.github.shynixn.mccoroutine.bukkit.*
import com.github.shynixn.mccoroutine.bukkit.sample.commandexecutor.AdminCommandExecutor
import com.github.shynixn.mccoroutine.bukkit.sample.impl.FakeDatabase
import com.github.shynixn.mccoroutine.bukkit.sample.impl.UserDataCache
import com.github.shynixn.mccoroutine.bukkit.sample.listener.PlayerConnectListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit

class MCCoroutineSamplePlugin : SuspendingJavaPlugin() {
    /**
     * Called when this plugin is enabled
     */
    override suspend fun onEnableAsync() {
        println("[MCCoroutineSamplePlugin/onEnableAsync] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")

        withContext(Dispatchers.IO) {
            println("[MCCoroutineSamplePlugin/onEnableAsync] Simulating data load Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")
            Thread.sleep(500)
        }

        val database = FakeDatabase()
        val cache = UserDataCache(this, database)

        // Extension to traditional registration.
        server.pluginManager.registerSuspendingEvents(PlayerConnectListener(this, cache), this)
        server.pluginManager.registerSuspendingEvents(
            com.github.shynixn.mccoroutine.bukkit.sample.listener.EntityInteractListener(
                cache
            ), this);

        val commandExecutor = AdminCommandExecutor(cache, this)
        this.getCommand("mccor")!!.setSuspendingExecutor(commandExecutor)
        this.getCommand("mccor")!!.setSuspendingTabCompleter(commandExecutor)

        println("[MCCoroutineSamplePlugin/onEnableAsync] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")    }

    /**
     * Called when this plugin is disabled.
     */
    override suspend fun onDisableAsync() {
        println("[MCCoroutineSamplePlugin/onDisableAsync] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")

        // Do not use asyncDispatcher as it is already disposed at this point.
        withContext(Dispatchers.IO) {
            println("[MCCoroutineSamplePlugin/onDisableAsync] Simulating data save on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")
            Thread.sleep(500)
        }

        println("[MCCoroutineSamplePlugin/onDisableAsync] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")
    }
}
