package com.github.shynixn.mccoroutine.service

import com.github.shynixn.mccoroutine.contract.CommandService
import com.github.shynixn.mccoroutine.contract.CoroutineSession
import com.github.shynixn.mccoroutine.contract.EventService
import com.github.shynixn.mccoroutine.dispatcher.AsyncCoroutineDispatcher
import com.github.shynixn.mccoroutine.dispatcher.MinecraftCoroutineDispatcher
import com.github.shynixn.mccoroutine.minecraftDispatcher
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.spongepowered.api.plugin.PluginContainer
import kotlin.coroutines.CoroutineContext

internal class CoroutineSessionImpl(private val plugin: PluginContainer, private val logger: Logger) :
    CoroutineSession {
    private var disposed = false

    /**
     * Gets the scope.
     */
    override val scope: CoroutineScope by lazy {
        CoroutineScope(plugin.minecraftDispatcher)
    }

    /**
     * Gets the command service.
     */
    override val commandService: CommandService by lazy {
        CommandServiceImpl(plugin, this)
    }

    /**
     * Gets the event service.
     */
    override val eventService: EventService by lazy {
        EventServiceImpl(plugin, this)
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
                logger.debug("Coroutine has been cancelled.")
            } catch (e: Exception) {
                logger.error(
                    "This is not an error of MCCoroutine! See sub exception for details.",
                    e
                )
            }
        }
    }
}
