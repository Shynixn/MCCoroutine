package com.github.shynixn.mccoroutine.fabric.server

import com.github.shynixn.mccoroutine.fabric.MCCoroutineExceptionEvent
import com.github.shynixn.mccoroutine.fabric.executesSuspend
import com.github.shynixn.mccoroutine.fabric.launch
import com.github.shynixn.mccoroutine.fabric.mcCoroutineConfiguration
import com.github.shynixn.mccoroutine.fabric.server.command.AdminCommandExecutor
import com.github.shynixn.mccoroutine.fabric.server.impl.FakeDatabase
import com.github.shynixn.mccoroutine.fabric.server.impl.UserDataCache
import com.github.shynixn.mccoroutine.fabric.server.listener.PlayerListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandManager
import net.minecraft.util.ActionResult
import java.util.concurrent.Executor

class MCCoroutineSampleServerMod : DedicatedServerModInitializer {
    /**
     * Runs the mod initializer on the server environment.
     */
    override fun onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTING.register(ServerLifecycleEvents.ServerStarting { server ->
            // Connect Native Minecraft Scheduler and MCCoroutine.
            mcCoroutineConfiguration.minecraftExecutor = Executor { r ->
                server.submitAndJoin(r)
            }
            launch {
                onServerStarting(server)
            }
        })

        ServerLifecycleEvents.SERVER_STOPPING.register { server ->
            mcCoroutineConfiguration.disposePluginSession()
        }
    }

    /**
     * MCCoroutine is ready after the server has started.
     */
    private suspend fun onServerStarting(server : MinecraftServer) {
        println("[MCCoroutineSample/main] MainThread 1 Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
        delay(2000)
        println("[MCCoroutineSample/main] MainThread 2 Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")

        withContext(Dispatchers.IO) {
            println("[MCCoroutineSample/main] Simulating data load Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            Thread.sleep(500)
        }
        println("[MCCoroutineSample/main] MainThread 3 Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")

        val database = FakeDatabase()
        val cache = UserDataCache(this, database)

        // Register command
        val command = AdminCommandExecutor(
            cache
        )
        server.commandManager.dispatcher.register(CommandManager.literal("mccor").executesSuspend(this, command))

        // Register listener
        val listener = PlayerListener(this, cache)
        val mod = this
        AttackEntityCallback.EVENT.register(AttackEntityCallback { player, world, hand, entity, hitResult ->
            mod.launch {
                listener.onPlayerAttackEvent(player, world, hand, entity, hitResult)
            }
            ActionResult.PASS
        })
        MCCoroutineExceptionEvent.EVENT.register(object : MCCoroutineExceptionEvent {
            override fun onMCCoroutineException(throwable: Throwable, entryPoint: Any): Boolean {
                return listener.onCoroutineException(throwable, entryPoint)
            }
        })
    }
}
