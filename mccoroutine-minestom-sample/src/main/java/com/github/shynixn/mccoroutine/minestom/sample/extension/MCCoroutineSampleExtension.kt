package com.github.shynixn.mccoroutine.minestom.sample.extension

import com.github.shynixn.mccoroutine.minestom.MCCoroutineExceptionEvent
import com.github.shynixn.mccoroutine.minestom.addSuspendingListener
import com.github.shynixn.mccoroutine.minestom.launch
import com.github.shynixn.mccoroutine.minestom.mcCoroutineConfiguration
import com.github.shynixn.mccoroutine.minestom.sample.extension.commandexecutor.AdminCommandExecutor
import com.github.shynixn.mccoroutine.minestom.sample.extension.impl.FakeDatabase
import com.github.shynixn.mccoroutine.minestom.sample.extension.impl.UserDataCache
import com.github.shynixn.mccoroutine.minestom.sample.extension.listener.PlayerConnectListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.extensions.Extension

/**
 * Minestom can either be customized on server level or on extension level. MCCoroutine
 * implements scope handling for both types. This is the MCCoroutine Extension Scope.
 */
class MCCoroutineSampleExtension : Extension() {
    /**
     * Gets called on extension load.
     */
    override fun initialize() {
        println("[MCCoroutineSampleExtension/main] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")

        // Switches into suspendable scope on startup.
        this.launch {
            println("[MCCoroutineSampleExtension/main] MainThread 1 Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            delay(2000)
            println("[MCCoroutineSampleExtension/main] MainThread 2 Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")

            withContext(Dispatchers.IO) {
                println("[MCCoroutineSampleExtension/main] Simulating data load Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
                Thread.sleep(500)
            }
            println("[MCCoroutineSampleExtension/main] MainThread 3 Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
        }

        val database = FakeDatabase()
        val cache = UserDataCache(this, database)

        // Extension to traditional registration.
        val playerConnectListener = PlayerConnectListener(this, cache)
        val rootEventNode = MinecraftServer.getGlobalEventHandler()
        val mine = MinecraftServer.init()
        rootEventNode.addSuspendingListener(mine, AsyncPlayerConfigurationEvent::class.java) { e ->
            playerConnectListener.onPlayerJoinEvent(e)
        }
        rootEventNode.addSuspendingListener(mine, PlayerDisconnectEvent::class.java) { e ->
            playerConnectListener.onPlayerQuitEvent(e)
        }
        rootEventNode.addSuspendingListener(mine, MCCoroutineExceptionEvent::class.java) { e ->
            playerConnectListener.onCoroutineException(e)
        }

        MinecraftServer.getCommandManager().register(
            AdminCommandExecutor(
                cache,
                this
            )
        )
    }

    /**
     * Gets called on extension disable.
     */
    override fun terminate() {
        // Minestom requires manual disposing.
        this.mcCoroutineConfiguration.disposePluginSession()
    }
}
