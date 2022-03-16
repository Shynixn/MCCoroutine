package com.github.shynixn.mccoroutine.bukkit.impl

import com.github.shynixn.mccoroutine.bukkit.dispatcher.AsyncCoroutineDispatcher
import com.github.shynixn.mccoroutine.bukkit.dispatcher.MinecraftCoroutineDispatcher
import com.github.shynixn.mccoroutine.bukkit.internal.CommandService
import com.github.shynixn.mccoroutine.bukkit.internal.CoroutineSession
import com.github.shynixn.mccoroutine.bukkit.internal.EventService
import com.github.shynixn.mccoroutine.bukkit.internal.WakeUpBlockService
import com.github.shynixn.mccoroutine.bukkit.service.CommandServiceImpl
import com.github.shynixn.mccoroutine.bukkit.service.EventServiceImpl
import com.github.shynixn.mccoroutine.bukkit.service.WakeUpBlockServiceImpl
import kotlinx.coroutines.*
import org.bukkit.plugin.Plugin
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext

internal class CoroutineSessionImpl(private val plugin: Plugin) : CoroutineSession {
    /**
     * Gets minecraft coroutine scope.
     */
    override val scope: CoroutineScope

    /**
     * Gets the minecraft dispatcher.
     */
    override val dispatcherMinecraft: CoroutineContext by lazy {
        MinecraftCoroutineDispatcher(plugin, wakeUpBlockService)
    }

    /**
     * Gets the async dispatcher.
     */
    override val dispatcherAsync: CoroutineContext by lazy {
        AsyncCoroutineDispatcher(plugin, wakeUpBlockService)
    }

    /**
     * Gets the block service during startup.
     */
    override val wakeUpBlockService: WakeUpBlockService by lazy {
        WakeUpBlockServiceImpl(plugin)
    }

    /**
     * Gets the event service.
     */
    override val eventService: EventService by lazy {
        EventServiceImpl(plugin)
    }

    /**
     * Gets the command service.
     */
    override val commandService: CommandService by lazy {
        CommandServiceImpl(plugin)
    }

    init {
        // Root Exception Handler. All Exception which are not consumed by the caller end up here.
        val exceptionHandler = CoroutineExceptionHandler { _, e ->
            if (e is CancellationException) {
                plugin.logger.log(Level.INFO, "Coroutine has been cancelled.")
            } else {
                plugin.logger.log(
                    Level.SEVERE,
                    "This is not an error of MCCoroutine! See sub exception for details.",
                    e
                )
            }
        }

        // Build Coroutine plugin scope for exception handling
        val rootCoroutineScope = CoroutineScope(exceptionHandler)

        // Minecraft Scope is child of plugin scope and super visor job (e.g. children of a supervisor job can fail independently).
        scope = rootCoroutineScope + SupervisorJob() + dispatcherMinecraft
    }

    /**
     * Launches the given function on the plugin coroutine scope.
     * @return Cancelable coroutine job.
     */
    override fun launch(
        context: CoroutineContext,
        start: CoroutineStart,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        if (!scope.isActive) {
            return Job()
        }

        return scope.launch(context, start, block)
    }

    /**
     * Disposes the session.
     */
    override fun dispose() {
        scope.cancel()
        wakeUpBlockService.dispose()
    }
}
