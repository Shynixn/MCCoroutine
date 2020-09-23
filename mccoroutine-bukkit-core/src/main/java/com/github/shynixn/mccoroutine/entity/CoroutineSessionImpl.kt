package com.github.shynixn.mccoroutine.entity

import com.github.shynixn.mccoroutine.asyncDispatcher
import com.github.shynixn.mccoroutine.contract.CommandService
import com.github.shynixn.mccoroutine.contract.CoroutineSession
import com.github.shynixn.mccoroutine.contract.EventService
import com.github.shynixn.mccoroutine.dispatcher.AsyncCoroutineDispatcher
import com.github.shynixn.mccoroutine.dispatcher.MinecraftCoroutineDispatcher
import com.github.shynixn.mccoroutine.minecraftDispatcher
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ProducerScope
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Level
import kotlin.collections.HashMap
import kotlin.coroutines.CoroutineContext

internal class CoroutineSessionImpl(private val plugin: Plugin) : CoroutineSession {
    private val scope = CoroutineScope(plugin.minecraftDispatcher)
    private var disposed = false

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
    }

    /**
     * Launches the given function on the Minecraft Thread and handles
     * coroutine scopes correctly.
     */
    override fun launchOnMinecraft(f: suspend CoroutineScope.() -> Unit) {
        if (disposed) {
            return
        }

        // Launch a new coroutine on the minecraft thread on the plugin scope.
        scope.launch(plugin.minecraftDispatcher) {
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

    /**
     * Launches the given function on an Async Thread and handles
     * coroutine scopes correctly.
     */
    override fun launchOnAsync(f: suspend CoroutineScope.() -> Unit) {
        // Launch a new coroutine on the minecraft thread on the plugin scope.
        scope.launch(plugin.asyncDispatcher) {
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
