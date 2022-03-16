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
        println("[MCCoroutineSamplePlugin] OnEnable on Primary Thread: " + Bukkit.isPrimaryThread())

        withContext(this.asyncDispatcher) {
            println("[MCCoroutineSamplePlugin] Loading some data on async Thread: " + Bukkit.isPrimaryThread())
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

        println("[MCCoroutineSamplePlugin] OnEnabled End on Primary Thread: " + Bukkit.isPrimaryThread())
    }

    /**
     * Called when this plugin is disabled.
     */
    override suspend fun onDisableAsync() {
        println("[MCCoroutineSamplePlugin] OnDisable on Primary Thread " + Bukkit.isPrimaryThread())

        // Do not use asyncDispatcher as it is already disposed at this point.
        withContext(Dispatchers.IO) {
            println("[MCCoroutineSamplePlugin] Storing player data before shutting down...." + Bukkit.isPrimaryThread())
            Thread.sleep(500)
        }

        println("[MCCoroutineSamplePlugin] Completed shutting down." + Bukkit.isPrimaryThread())
    }
}
