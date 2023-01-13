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
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerEntityInteractEvent
import net.minestom.server.event.player.PlayerLoginEvent
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
        println("[MCCoroutineSampleServer/main] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")

        // Switches into suspendable scope on startup.
        this.launch {
            println("[MCCoroutineSampleServer/main] MainThread 1 Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            delay(2000)
            println("[MCCoroutineSampleServer/main] MainThread 2 Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")

            withContext(Dispatchers.IO) {
                println("[MCCoroutineSampleServer/main] Simulating data load Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
                Thread.sleep(500)
            }
            println("[MCCoroutineSampleServer/main] MainThread 3 Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
        }

        val database = FakeDatabase()
        val cache = UserDataCache(this, database)

        // Extension to traditional registration.
        val playerConnectListener = PlayerConnectListener(this, cache)
        val rootEventNode = EventNode.all("my-root")
        rootEventNode.addSuspendingListener(this, PlayerLoginEvent::class.java) { e ->
            playerConnectListener.onPlayerJoinEvent(e)
        }
        rootEventNode.addSuspendingListener(this, PlayerDisconnectEvent::class.java) { e ->
            playerConnectListener.onPlayerQuitEvent(e)
        }
        rootEventNode.addSuspendingListener(this, MCCoroutineExceptionEvent::class.java) { e ->
            playerConnectListener.onCoroutineException(e)
        }

        AdminCommandExecutor(cache, this)
    }

    /**
     * Gets called on extension disable.
     */
    override fun terminate() {
        // Minestom requires manual disposing.
        this.mcCoroutineConfiguration.disposePluginSession()
    }
}
