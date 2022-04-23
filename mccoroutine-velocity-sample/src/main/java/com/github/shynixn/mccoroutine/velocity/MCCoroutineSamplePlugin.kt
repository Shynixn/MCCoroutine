package com.github.shynixn.mccoroutine.velocity

import com.github.shynixn.mccoroutine.velocity.commandexecutor.AdminCommandExecutor
import com.github.shynixn.mccoroutine.velocity.impl.FakeDatabase
import com.github.shynixn.mccoroutine.velocity.impl.UserDataCache
import com.github.shynixn.mccoroutine.velocity.listener.PlayerConnectListener
import com.github.shynixn.mccoroutine.velocity.listener.PlayerDisconnectListener
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Plugin(
    id = "mccoroutinesample",
    name = "MCCoroutineSample",
    description = "MCCoroutineSample is sample plugin to use MCCoroutine in Velocity."
)
class MCCoroutineSamplePlugin {
    @Inject
    lateinit var proxyServer: ProxyServer

    @Subscribe
    suspend fun onProxyInitialization(event: ProxyInitializeEvent) {
        println("[MCCoroutineSamplePlugin/onProxyInitialization] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")

        withContext(Dispatchers.IO) {
            println("[MCCoroutineSamplePlugin/onProxyInitialization] loading some data on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            Thread.sleep(500) // Simulate data loading.
        }

        println("[MCCoroutineSamplePlugin/onProxyInitialization] Is continuing on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")

        val database = FakeDatabase()
        val cache = UserDataCache(this as PluginContainer, database)

        // Extension to traditional registration.
        proxyServer.eventManager.registerSuspend(this, PlayerConnectListener(cache))
        proxyServer.eventManager.registerSuspend(this, PlayerDisconnectListener(cache))

        val commandExecutor = AdminCommandExecutor(cache)
        val meta = proxyServer.commandManager.metaBuilder("test")
        proxyServer.commandManager.registerSuspend(meta.build(),commandExecutor, this)
    }

    @Subscribe
    suspend fun onProxyShutdown(event: ProxyShutdownEvent) {
        println("[MCCoroutineSamplePlugin/onProxyShutdown] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")

        withContext(Dispatchers.IO) {
            println("[MCCoroutineSamplePlugin/onProxyShutdown] Simulate storing data on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            Thread.sleep(500)
        }

        println("[MCCoroutineSamplePlugin/onProxyShutdown] is shutting down. Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
    }
}
