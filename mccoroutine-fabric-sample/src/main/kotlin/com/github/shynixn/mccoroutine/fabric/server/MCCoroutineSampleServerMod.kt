package com.github.shynixn.mccoroutine.fabric.server

import com.github.shynixn.mccoroutine.fabric.launch
import com.github.shynixn.mccoroutine.fabric.mcCoroutineConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import java.util.concurrent.Executor

class MCCoroutineSampleServerMod : DedicatedServerModInitializer {
    /**
     * Runs the mod initializer on the server environment.
     */
    override fun onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTING.register(ServerLifecycleEvents.ServerStarting { server ->
            mcCoroutineConfiguration.minecraftExecutor = Executor { r ->
                server.submitAndJoin(r)
            }
            onServerStarting()
        })
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerLifecycleEvents.ServerStopping{ server ->
            mcCoroutineConfiguration.disposePluginSession()
        })
    }

    /**
     * MCCoroutine is ready after the server has started.
     */
    fun onServerStarting() {
        launch {
            println("[MCCoroutineSampleServer/main] MainThread 1 Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            delay(2000)
            println("[MCCoroutineSampleServer/main] MainThread 2 Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")

            withContext(Dispatchers.IO) {
                println("[MCCoroutineSampleServer/main] Simulating data load Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
                Thread.sleep(500)
            }
            println("[MCCoroutineSampleServer/main] MainThread 3 Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
        }
    }
}
