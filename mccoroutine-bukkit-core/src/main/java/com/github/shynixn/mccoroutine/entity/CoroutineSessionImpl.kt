package com.github.shynixn.mccoroutine.entity

import com.github.shynixn.mccoroutine.contract.CommandService
import com.github.shynixn.mccoroutine.contract.CoroutineSession
import com.github.shynixn.mccoroutine.contract.EventService
import com.github.shynixn.mccoroutine.contract.ProtocolService
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
        EventServiceImpl(plugin)
    }

    /**
     * Gets the protocol service.
     */
    override val protocolService: ProtocolService by lazy {
        ProtocolServiceImpl(plugin)
    }

    /**
     * Gets the command service.
     */
    override val commandService: CommandService by lazy {
        CommandServiceImpl(plugin, this)
    }

    /**
     * Gets the minecraft dispatcher.
     */
    override val dispatcherMinecraft: CoroutineContext by lazy {
        MinecraftCoroutineDispatcher(plugin)
    }

    /**
     * Gets the async dispatcher.
     */
    override val dispatcherAsync: CoroutineContext by lazy {
        AsyncCoroutineDispatcher(plugin)
    }

    /**
     * Disposes the session.
     */
    override fun dispose() {
        disposed = true
        scope.coroutineContext.cancelChildren()
        protocolService.dispose()
    }

    /**
     * Launches the given function on the plugin coroutine scope.
     */
    override fun launch(dispatcher: CoroutineContext, f: suspend CoroutineScope.() -> Unit) {
        if (disposed) {
            return
        }

        // Launch a new coroutine on the current thread thread on the plugin scope.
        scope.launch(dispatcher) {
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
