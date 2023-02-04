package com.github.shynixn.mccoroutine.fabric.impl

import com.github.shynixn.mccoroutine.fabric.MCCoroutine
import com.github.shynixn.mccoroutine.fabric.MCCoroutineConfiguration
import com.github.shynixn.mccoroutine.fabric.ShutdownStrategy
import java.util.concurrent.Executor
import java.util.logging.Logger


internal class MCCoroutineConfigurationImpl(private val extension: Any, private val mcCoroutine: MCCoroutine) :
    MCCoroutineConfiguration {
    /**
     * Strategy handling how MCCoroutine is disposed.
     * Defaults to ShutdownStrategy.MANUAL.
     */
    override var shutdownStrategy: ShutdownStrategy = ShutdownStrategy.MANUAL

    /**
     * The executor being used to schedule tasks on the main thread of minecraft.
     * Can be retrieved from the MinecraftServer instance.
     */
    override var minecraftExecutor: Executor = Executor {
        throw RuntimeException("You need to set the minecraft scheduler to MCCoroutine. e.g. ServerLifecycleEvents.SERVER_STARTING.register(ServerLifecycleEvents.ServerStarting { server ->  mcCoroutineConfiguration.minecraftExecutor = Executor { r -> server.submitAndJoin(r)}})")
    }

    /**
     * The logger being used by MCCoroutine.
     */
    override var logger: Logger = Logger.getLogger(extension.javaClass.simpleName)

    /**
     * Manually disposes the MCCoroutine session for the given plugin.
     */
    override fun disposePluginSession() {
        mcCoroutine.disable(extension)
    }
}
