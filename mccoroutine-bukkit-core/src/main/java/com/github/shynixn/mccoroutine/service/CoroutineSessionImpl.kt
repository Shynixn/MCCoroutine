package com.github.shynixn.mccoroutine.service

import com.github.shynixn.mccoroutine.contract.CommandService
import com.github.shynixn.mccoroutine.contract.CoroutineSession
import com.github.shynixn.mccoroutine.contract.EventService
import com.github.shynixn.mccoroutine.contract.WakeUpBlockService
import com.github.shynixn.mccoroutine.dispatcher.AsyncCoroutineDispatcher
import com.github.shynixn.mccoroutine.dispatcher.MinecraftCoroutineDispatcher
import com.github.shynixn.mccoroutine.minecraftDispatcher
import kotlinx.coroutines.*
import org.bukkit.plugin.Plugin
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext

internal class CoroutineSessionImpl(private val plugin: Plugin) : CoroutineSession {
    private var disposed = false

    /**
     * Gets the scope.
     */
    override val scope: CoroutineScope by lazy {
        CoroutineScope(plugin.minecraftDispatcher)
    }

    /**
     * Gets the event service.
     */
    override val eventService: EventService by lazy {
        EventServiceImpl(plugin, this)
    }

    /**
     * Gets the command service.
     */
    override val commandService: CommandService by lazy {
        CommandServiceImpl(plugin, this)
    }

    /**
     * Gets the wakeup service.
     */
    override val wakeUpBlockService: WakeUpBlockService by lazy {
        WakeUpBlockServiceImpl(plugin)
    }

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
     * Disposes the session.
     */
    override fun dispose() {
        disposed = true
        scope.coroutineContext.cancelChildren()
        wakeUpBlockService.dispose()
    }

    /**
     * Launches the given function on the plugin coroutine scope.
     */
    override fun launch(dispatcher: CoroutineContext, f: suspend CoroutineScope.() -> Unit): Job {
        if (disposed) {
            return Job()
        }

        if (dispatcher == Dispatchers.Unconfined) {
            // If the dispatcher is unconfined. Always schedule immediately.
            return launchInternal(dispatcher, CoroutineStart.UNDISPATCHED, f)
        }

        return launchInternal(dispatcher, CoroutineStart.DEFAULT, f)
    }

    /**
     * Executes the launch
     */
    private fun launchInternal(
        dispatcher: CoroutineContext,
        coroutineStart: CoroutineStart,
        f: suspend CoroutineScope.() -> Unit
    ): Job {
        // Launch a new coroutine on the current thread thread on the plugin scope.
        return scope.launch(dispatcher, coroutineStart) {
            try {
                // The user may or may not launch multiple sub suspension operations. If
                // one of those fails, only this scope should fail instead of the plugin scope.
                coroutineScope {
                    f.invoke(this)
                }
            } catch (e: CancellationException) {
                plugin.logger.log(Level.INFO, "Coroutine has been cancelled.")
            } catch (e: Exception) {
                plugin.logger.log(
                    Level.SEVERE,
                    "This is not an error of MCCoroutine! See sub exception for details.",
                    e
                )
            }
        }
    }
}
