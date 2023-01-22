package com.github.shynixn.mccoroutine.fabric

import java.util.concurrent.Executor
import java.util.logging.Logger

/**
 * Additional configurations for MCCoroutine and communication.
 */
interface MCCoroutineConfiguration {
    /**
     * Strategy handling how MCCoroutine is disposed.
     * Defaults to ShutdownStrategy.SCHEDULER.
     *
     * Changing this setting may have an impact on All suspend function you call in
     * onDisable(). Carefully verify your changes.
     */
    var shutdownStrategy: ShutdownStrategy

    /**
     * The executor being used to schedule tasks on the main thread of minecraft.
     * Can be retrieved from the MinecraftServer instance.
     */
    var minecraftExecutor: Executor

    /**
     * The logger being used by MCCoroutine.
     */
    var logger: Logger

    /**
     * Manually disposes the MCCoroutine session for the current extension or server.
     */
    fun disposePluginSession()
}
