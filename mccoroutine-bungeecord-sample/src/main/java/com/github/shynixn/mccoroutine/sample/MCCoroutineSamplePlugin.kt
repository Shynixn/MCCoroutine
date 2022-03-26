package com.github.shynixn.mccoroutine.sample

import com.github.shynixn.mccoroutine.*
import com.github.shynixn.mccoroutine.sample.commandexecutor.AdminCommandExecutor
import com.github.shynixn.mccoroutine.sample.impl.FakeDatabase
import com.github.shynixn.mccoroutine.sample.impl.UserDataCache
import com.github.shynixn.mccoroutine.sample.listener.PlayerConnectListener
import com.github.shynixn.mccoroutine.sample.listener.PlayerDisconnectListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MCCoroutineSamplePlugin : SuspendingPlugin() {
    /**
     * Called when this plugin is enabled
     */
    override suspend fun onEnableAsync() {
        println("[MCCoroutineSamplePlugin/onEnableAsync] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")

        withContext(Dispatchers.IO) {
            println("[MCCoroutineSamplePlugin/onEnableAsync] loading some data on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            Thread.sleep(500) // Simulate data loading.
        }

        println("[MCCoroutineSamplePlugin/onEnableAsync] Is continuing on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")

        val database = FakeDatabase()
        val cache = UserDataCache(this, database)

        // Extension to traditional registration.
        proxy.pluginManager.registerSuspendingListener(this, PlayerConnectListener(this, cache))
        proxy.pluginManager.registerListener(this, PlayerDisconnectListener(cache));

        val commandExecutor = AdminCommandExecutor(cache, this)
        proxy.pluginManager.registerSuspendingCommand(this, commandExecutor)
    }

    /**
     * Called when this plugin is disabled.
     */
    override suspend fun onDisableAsync() {
        println("[MCCoroutineSamplePlugin/ onDisableAsync] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")

        // Async Dispatcher is the same as the Dispatchers.IO in the BungeeCord implementation.
        withContext(Dispatchers.IO) {
            println("[MCCoroutineSamplePlugin/ onDisableAsync] Simulate storing data on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            Thread.sleep(500)
        }

        println("[MCCoroutineSamplePlugin/ onDisableAsync] is shutting down. Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
    }
}
