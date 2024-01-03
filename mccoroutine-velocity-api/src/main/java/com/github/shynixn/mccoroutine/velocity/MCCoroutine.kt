package com.github.shynixn.mccoroutine.velocity

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.CommandManager
import com.velocitypowered.api.command.CommandMeta
import com.velocitypowered.api.event.AwaitingEventExecutor
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.plugin.PluginContainer
import kotlinx.coroutines.*
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

/**
 * Static session.
 */
internal val mcCoroutine: MCCoroutine by lazy {
    try {
        Class.forName("com.github.shynixn.mccoroutine.velocity.impl.MCCoroutineImpl")
            .getDeclaredConstructor().newInstance() as MCCoroutine
    } catch (e: Exception) {
        throw RuntimeException(
            "Failed to load MCCoroutine implementation. Shade mccoroutine-velocity-core into your plugin.",
            e
        )
    }
}

/**
 * Gets the plugin velocity dispatcher.
 */
val PluginContainer.velocityDispatcher: CoroutineContext
    get() {
        return mcCoroutine.getCoroutineSession(this).dispatcherVelocity
    }

/**
 * Gets the plugin coroutine scope.
 */
val PluginContainer.scope: CoroutineScope
    get() {
        return mcCoroutine.getCoroutineSession(this).scope
    }

/**
 * Launches a new coroutine on the Velocity Plugin ThreadPool without blocking the current thread and returns a reference to the coroutine as a [Job].
 * The coroutine is cancelled when the resulting job is [cancelled][Job.cancel].
 *
 * The coroutine context is inherited from a [PluginContainer.scope]. Additional context elements can be specified with [context] argument.
 * If the context does not have any dispatcher nor any other [ContinuationInterceptor], then [PluginContainer.velocityDispatcher] is used.
 * The parent job is inherited from a [PluginContainer.scope] as well, but it can also be overridden
 * with a corresponding [context] element.
 *
 * By default, the coroutine is scheduled on the Velocity thread pool which is executed later.
 * Other start options can be specified via `start` parameter. See [CoroutineStart] for details.
 * An optional [start] parameter can be set to [CoroutineStart.LAZY] to start coroutine _lazily_. In this case,
 * the coroutine [Job] is created in _new_ state. It can be explicitly started with [start][Job.start] function
 * and will be started implicitly on the first invocation of [join][Job.join].
 *
 * Uncaught exceptions in this coroutine do not cancel the parent job or any other child jobs. All uncaught exceptions
 * are logged to the plugin logger. by default.
 *
 * @param context The coroutine context to start. Should almost be always be [PluginContainer.velocityDispatcher]. Async operations should be
 * be created using [withContext] after using the default parameters of this method.
 * @param start coroutine start option. The default value is [CoroutineStart.DEFAULT].
 * @param block the coroutine code which will be invoked in the context of the provided scope.
 **/
fun PluginContainer.launch(
    context: CoroutineContext = velocityDispatcher,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    if (!scope.isActive) {
        return Job()
    }

    return scope.launch(context, start, block)
}


/**
 * Registers an event listener with suspending functions.
 * Does exactly the same thing as server.getEventManager().register but makes suspension functions
 * possible.
 * Example:
 *
 * class MyListener {
 *     @Subscribe
 *     suspend fun onLoginEvent(event: PostLoginEvent) {
 *
 *     }
 * }
 *
 * @param listener Velocity Listener.
 * @param plugin Velocity Plugin.
 */
fun EventManager.registerSuspend(plugin: Any, listener: Any) {
    return mcCoroutine.getCoroutineSession(plugin).registerSuspendListener(listener, false)
}

/**
 * Registers a new event listener with a functional style listener.
 *
 * @param plugin Velocity Plugin.
 * @param eventClass Velocity Event class
 * @param handler suspend invocation
 */
fun <E> EventManager.registerSuspend(
    plugin: Any,
    eventClass: Class<E>,
    handler: suspend (handler: E) -> Unit
) {
    return registerSuspend(plugin, eventClass, PostOrder.NORMAL, handler)
}

/**
 * Registers a new event listener with a functional style listener.
 *
 * @param plugin Velocity Plugin.
 * @param eventClass Velocity Event class
 * @param postOrder postOrder parameter,
 * @param handler suspend invocation
 */
fun <E> EventManager.registerSuspend(
    plugin: Any,
    eventClass: Class<E>,
    postOrder: PostOrder,
    handler: suspend (handler: E) -> Unit
) {
    val session = mcCoroutine.getCoroutineSession(plugin)
    val scope = session.scope
    val dispatcher = session.dispatcherVelocity

    this.register(plugin, eventClass, postOrder, object : AwaitingEventExecutor<E> {
        override fun executeAsync(event: E): EventTask {
            return EventTask.withContinuation { continuation ->
                // Start unDispatched on the same thread but end up on the velocity dispatcher.
                scope.launch(dispatcher, CoroutineStart.UNDISPATCHED) {
                    handler.invoke(event)
                    continuation.resume()
                }
            }
        }
    });
}

/**
 * Allows to register a suspending command.
 */
fun <S, T : ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.executesSuspend(
    plugin: Any,
    command: suspend (context: CommandContext<S>) -> Int
): T {
    val session = mcCoroutine.getCoroutineSession(plugin)
    val scope = session.scope
    val dispatcher = session.dispatcherVelocity

    val result = this.executes({ commandContext ->
        // Start unDispatched on the same thread but end up on the velocity dispatcher.
        scope.launch(dispatcher, CoroutineStart.UNDISPATCHED) {
            command.invoke(commandContext)
        }

        com.mojang.brigadier.Command.SINGLE_SUCCESS
    })!!

    return result
}

/**
 * Registers an command executor with suspending function.
 * Does exactly the same as CommandManager.register
 * @param meta CommandMeta.
 * @param plugin Velocity Plugin.
 * @param command SuspendingCommand.
 */
fun CommandManager.registerSuspend(
    meta: CommandMeta,
    command: SuspendingSimpleCommand,
    plugin: Any
) {
    return mcCoroutine.getCoroutineSession(plugin).registerSuspendCommand(meta, command)
}

interface MCCoroutine {
    /**
     * Get coroutine session for the given plugin.
     * @param plugin can be a plugin instance or pluginContainer instance.
     */
    fun getCoroutineSession(plugin: Any): CoroutineSession

    /**
     * Configures the suspending plugin container with the real plugin Container.
     */
    fun setupCoroutineSession(
        pluginInstance: Any,
        pluginContainer: PluginContainer,
        suspendingPluginContainer: SuspendingPluginContainer
    )

    /**
     * Disables logging false positives.
     */
    fun disableLogging(plugin: PluginContainer, suspendingPluginContainer: SuspendingPluginContainer)

    /**
     * Disables coroutine for the given plugin.
     */
    fun disable(plugin: PluginContainer)
}
