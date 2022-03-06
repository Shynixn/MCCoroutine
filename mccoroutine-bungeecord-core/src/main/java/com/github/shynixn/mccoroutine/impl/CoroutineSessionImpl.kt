package com.github.shynixn.mccoroutine.impl

import com.github.shynixn.mccoroutine.bungeeCordDispatcher
import com.github.shynixn.mccoroutine.contract.CommandService
import com.github.shynixn.mccoroutine.contract.CoroutineSession
import com.github.shynixn.mccoroutine.contract.EventService
import com.github.shynixn.mccoroutine.dispatcher.BungeeCordCoroutineDispatcher
import com.github.shynixn.mccoroutine.service.BungeeCordCommandServiceImpl
import com.github.shynixn.mccoroutine.service.BungeeCordEventServiceImpl
import kotlinx.coroutines.*
import net.md_5.bungee.api.plugin.Plugin
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext

internal class CoroutineSessionImpl(private val plugin: Plugin) : CoroutineSession {
    private var disposed = false

    /**
     * Gets the scope.
     */
    override val scope: CoroutineScope by lazy {
        CoroutineScope(plugin.bungeeCordDispatcher)
    }

    /**
     * Gets the event service.
     */
    override val eventService: EventService by lazy {
        BungeeCordEventServiceImpl(plugin, this)
    }

    /**
     * Gets the command service.
     */
    override val commandService: CommandService by lazy {
        BungeeCordCommandServiceImpl(plugin, this)
    }

    /**
     * Gets the bungeeCord dispatcher.
     */
    override val dispatcherBungeeCord: CoroutineContext
            by lazy {
                BungeeCordCoroutineDispatcher(plugin)
            }

    /**
     * Gets the unconfined dispatcher.
     * This dispatcher is only being used internally to bridge certain executions.
     */
    override val unconfinedDispatcherBungeeCord: CoroutineContext
            by lazy {
                BungeeCordCoroutineDispatcher(plugin)
            }

    /**
     * Disposes the session.
     */
    override fun dispose() {
        disposed = true
        scope.coroutineContext.cancelChildren()
    }

    /**
     * Launches the given function on the plugin coroutine scope.
     */
    override fun launch(dispatcher: CoroutineContext, f: suspend CoroutineScope.() -> Unit): Job {
        if (disposed) {
            return Job()
        }

        if (dispatcher == Dispatchers.Unconfined || dispatcher == unconfinedDispatcherBungeeCord) {
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
