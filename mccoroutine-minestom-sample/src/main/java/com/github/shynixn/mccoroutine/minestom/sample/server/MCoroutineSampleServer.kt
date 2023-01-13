package com.github.shynixn.mccoroutine.minestom.sample.server

import com.github.shynixn.mccoroutine.minestom.MCCoroutineExceptionEvent
import com.github.shynixn.mccoroutine.minestom.addSuspendingListener
import com.github.shynixn.mccoroutine.minestom.launch
import com.github.shynixn.mccoroutine.minestom.sample.server.commandexecutor.AdminCommandExecutor
import com.github.shynixn.mccoroutine.minestom.sample.server.impl.FakeDatabase
import com.github.shynixn.mccoroutine.minestom.sample.server.impl.UserDataCache
import com.github.shynixn.mccoroutine.minestom.sample.server.listener.PlayerConnectListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.generator.GenerationUnit
import java.util.*


/**
 * Minestom can either be customized on server level or on extension level. MCCoroutine
 * implements scope handling for both types. This is the MCCoroutine Server Scope.
 */
fun main(args: Array<String>) {
    val minecraftServer = MinecraftServer.init()
    println("[MCCoroutineSampleServer/main] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")

    // Switches into suspendable scope on startup.
    minecraftServer.launch {
        println("[MCCoroutineSampleServer/main] MainThread 1 Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
        delay(2000)
        println("[MCCoroutineSampleServer/main] MainThread 2 Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")

        withContext(Dispatchers.IO) {
            println("[MCCoroutineSampleServer/main] Simulating data load Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            Thread.sleep(500)
        }
        println("[MCCoroutineSampleServer/main] MainThread 3 Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
    }

    // Build a very basic instance
    val instanceContainer: InstanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer()
    instanceContainer.setGenerator { unit: GenerationUnit ->
        unit.modifier().fillHeight(0, 40, Block.STONE)
    }
    val globalEventHandler = MinecraftServer.getGlobalEventHandler()
    globalEventHandler.addListener(
        PlayerLoginEvent::class.java
    ) { event: PlayerLoginEvent ->
        val player: Player = event.player
        player.setPermissionLevel(2)
        event.setSpawningInstance(instanceContainer)
        player.setRespawnPoint(Pos(0.0, 42.0, 0.0))
    }

    val database = FakeDatabase()
    val cache = UserDataCache(minecraftServer, database)

    // Extension to traditional registration.
    val playerConnectListener = PlayerConnectListener(minecraftServer, cache)
    val rootEventNode = MinecraftServer.getGlobalEventHandler()
    rootEventNode.addSuspendingListener(minecraftServer, PlayerLoginEvent::class.java) { e ->
        playerConnectListener.onPlayerJoinEvent(e)
    }
    rootEventNode.addSuspendingListener(minecraftServer, PlayerDisconnectEvent::class.java) { e ->
        playerConnectListener.onPlayerQuitEvent(e)
    }
    rootEventNode.addSuspendingListener(minecraftServer, MCCoroutineExceptionEvent::class.java) { e ->
        playerConnectListener.onCoroutineException(e)
    }

    AdminCommandExecutor(cache, minecraftServer)

    println(MinecraftServer.VERSION_NAME)
    println(UUID.randomUUID().toString())

    minecraftServer.start("0.0.0.0", 25565)
}

