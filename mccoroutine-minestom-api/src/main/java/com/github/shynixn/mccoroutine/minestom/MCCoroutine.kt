package com.github.shynixn.mccoroutine.minestom

import kotlinx.coroutines.*
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.event.Event
import net.minestom.server.event.EventListener
import net.minestom.server.event.EventNode
import net.minestom.server.extensions.Extension
import net.minestom.server.thread.Acquirable
import net.minestom.server.thread.AcquirableCollection
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

/**
 * Static session.
 */
internal val mcCoroutine: MCCoroutine by lazy {
    try {
        Class.forName(MCCoroutine.Driver)
            .getDeclaredConstructor().newInstance() as MCCoroutine
    } catch (e: Exception) {
        throw RuntimeException(
            "Failed to load MCCoroutine implementation. Shade mccoroutine-minestom-core into your application.",
            e
        )
    }
}

/**
 * Gets the configuration instance of MCCoroutine.
 */
val Extension.mcCoroutineConfiguration: MCCoroutineConfiguration
    get() {
        return mcCoroutine.getCoroutineSession(this).mcCoroutineConfiguration
    }

/**
 * Gets the configuration instance of MCCoroutine.
 */
val MinecraftServer.mcCoroutineConfiguration: MCCoroutineConfiguration
    get() {
        return mcCoroutine.getCoroutineSession(this).mcCoroutineConfiguration
    }

/**
 * Gets the extension minecraft dispatcher.
 */
val Extension.minecraftDispatcher: CoroutineContext
    get() {
        return mcCoroutine.getCoroutineSession(this).dispatcherMinecraft
    }

/**
 * Gets the server minecraft dispatcher.
 */
val MinecraftServer.minecraftDispatcher: CoroutineContext
    get() {
        return mcCoroutine.getCoroutineSession(this).dispatcherMinecraft
    }

/**
 * Gets the extension async dispatcher.
 */
val Extension.asyncDispatcher: CoroutineContext
    get() {
        return mcCoroutine.getCoroutineSession(this).dispatcherAsync
    }

/**
 * Gets the server async dispatcher.
 */
val MinecraftServer.asyncDispatcher: CoroutineContext
    get() {
        return mcCoroutine.getCoroutineSession(this).dispatcherAsync
    }

/**
 * Gets the extension coroutine scope.
 */
val Extension.scope: CoroutineScope
    get() {
        return mcCoroutine.getCoroutineSession(this).scope
    }

/**
 * Gets the extension coroutine scope.
 */
val MinecraftServer.scope: CoroutineScope
    get() {
        return mcCoroutine.getCoroutineSession(this).scope
    }

/**
 * Launches a new coroutine on the minecraft main thread without blocking the current thread and returns a reference to the coroutine as a [Job].
 * The coroutine is cancelled when the resulting job is [cancelled][Job.cancel].
 *
 * The coroutine context is inherited from a [scope]. Additional context elements can be specified with [context] argument.
 * If the context does not have any dispatcher nor any other [ContinuationInterceptor], then [minecraftDispatcher] is used.
 * The parent job is inherited from a [scope] as well, but it can also be overridden
 * with a corresponding [context] element.
 *
 * By default, the coroutine is immediately scheduled for execution if the current thread is already the minecraft server thread.
 * If the current thread is not the minecraft server thread, the coroutine is moved to the [net.minestom.server.timer.Scheduler] and executed
 * in the next server tick schedule.
 * Other start options can be specified via `start` parameter. See [CoroutineStart] for details.
 * An optional [start] parameter can be set to [CoroutineStart.LAZY] to start coroutine _lazily_. In this case,
 * the coroutine [Job] is created in _new_ state. It can be explicitly started with [start][Job.start] function
 * and will be started implicitly on the first invocation of [join][Job.join].
 *
 * Uncaught exceptions in this coroutine do not cancel the parent job or any other child jobs. All uncaught exceptions
 * are logged to extension or server logger by default.
 *
 * @param context The coroutine context to start. Should almost be always be [minecraftDispatcher]. Async operations should be
 * be created using [withContext] after using the default parameters of this method.
 * @param start coroutine start option. The default value is [CoroutineStart.DEFAULT].
 * @param block the coroutine code which will be invoked in the context of the provided scope.
 **/
fun Extension.launch(
    context: CoroutineContext = minecraftDispatcher,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    if (!scope.isActive) {
        return Job()
    }

    return scope.launch(context, start, block)
}

/**
 * Launches a new coroutine on the minecraft main thread without blocking the current thread and returns a reference to the coroutine as a [Job].
 * The coroutine is cancelled when the resulting job is [cancelled][Job.cancel].
 *
 * The coroutine context is inherited from a [scope]. Additional context elements can be specified with [context] argument.
 * If the context does not have any dispatcher nor any other [ContinuationInterceptor], then [minecraftDispatcher] is used.
 * The parent job is inherited from a [scope] as well, but it can also be overridden
 * with a corresponding [context] element.
 *
 * By default, the coroutine is immediately scheduled for execution if the current thread is already the minecraft server thread.
 * If the current thread is not the minecraft server thread, the coroutine is moved to the [net.minestom.server.timer.Scheduler] and executed
 * in the next server tick schedule.
 * Other start options can be specified via `start` parameter. See [CoroutineStart] for details.
 * An optional [start] parameter can be set to [CoroutineStart.LAZY] to start coroutine _lazily_. In this case,
 * the coroutine [Job] is created in _new_ state. It can be explicitly started with [start][Job.start] function
 * and will be started implicitly on the first invocation of [join][Job.join].
 *
 * Uncaught exceptions in this coroutine do not cancel the parent job or any other child jobs. All uncaught exceptions
 * are logged to extension or server logger by default.
 *
 * @param context The coroutine context to start. Should almost be always be [minecraftDispatcher]. Async operations should be
 * be created using [withContext] after using the default parameters of this method.
 * @param start coroutine start option. The default value is [CoroutineStart.DEFAULT].
 * @param block the coroutine code which will be invoked in the context of the provided scope.
 **/
fun MinecraftServer.launch(
    context: CoroutineContext = minecraftDispatcher,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    if (!scope.isActive) {
        return Job()
    }

    return scope.launch(context, start, block)
}

/**
 * Converts the number to ticks for being used together with delay(..).
 * E.g. delay(1.ticks).
 * Minecraft ticks 20 times per second, which means a tick appears every 50 milliseconds. However,
 * delay() does not directly work with the MineStomScheduler and needs millisecond manipulation to
 * work as expected. Therefore, 1 tick does not equal 50 milliseconds when using this method standalone and only
 * sums up to 50 milliseconds if you use it together with delay.
 */
val Int.ticks: Long
    get() {
        return (this * 50L - 25)
    }

/**
 * Resolves the object (e.g. Entity) hidden inside the [Acquirable] object without blocking.
 * [Acquirable] are resolved immediately if already reserved for the current thread or resolved on a different
 * thread and the function is executed on it. You should only read/write the currently resolved object in the callback parameter. Do
 * not do anything like accessing fields or methods, do that outside of the callback parameter.
 * If you want to edit multiple [Acquirable] objects at once, put it into a collection [e.g. List, Set, etc.] and use Collection#asyncSuspend.
 */
suspend fun <T, R> Acquirable<T>.asyncSuspend(f: (T) -> R): R {
    val acquire = this
    return withContext(Dispatchers.IO) {
        var result: R? = null
        acquire.sync { element ->
            result = f.invoke(element)
        }
        result!!
    }
}

/**
 * Resolves the objects (e.g. Entities) hidden inside the [Acquirable] object without blocking.
 * [Acquirable] are resolved immediately if already reserved for the current thread or resolved on a different
 * thread and the function is executed on it. You should only read/write the currently resolved object in the callback parameter. Do
 * not do anything like accessing fields or methods, do that outside of the callback parameter.
 */
suspend fun <T, R> Collection<Acquirable<T>>.asyncSuspend(f: (Collection<T>) -> R): R {
    val acquirableCollection = AcquirableCollection(this)
    return withContext(Dispatchers.IO) {
        val resolved = ArrayList<T>()
        acquirableCollection.acquireSync {
            resolved.add(it)
        }
        f.invoke(resolved)
    }
}

/**
 * Sets the default {@link CommandExecutor}.
 *
 * @param server MineCraft server.
 * @param executor the new default executor.
 * @see #getDefaultExecutor()
 */
fun Command.setSuspendingDefaultExecutor(
    server: MinecraftServer,
    executor: suspend (CommandSender, CommandContext) -> Unit
) {
    this.setDefaultExecutor { sender: CommandSender, context ->
        server.launch {
            executor.invoke(sender, context)
        }
    }
}

/**
 * Sets the default {@link CommandExecutor}.
 *
 * @param server MineCraft server.
 * @param executor the new default executor.
 * @see #getDefaultExecutor()
 */
fun Command.setSuspendingDefaultExecutor(
    extension: Extension,
    executor: suspend (CommandSender, CommandContext) -> Unit
) {
    this.setDefaultExecutor { sender: CommandSender, context ->
        extension.launch {
            executor.invoke(sender, context)
        }
    }
}

/**
 * Adds a new suspendable listener to this event node.
 */
fun <E : Event> EventNode<in E>.addSuspendingListener(
    server: MinecraftServer,
    eventType: Class<E>,
    listener: suspend (E) -> Unit
) {
    this.addListener(eventType) { e ->
        server.launch {
            listener.invoke(e)
        }
    }
}

/**
 * Adds a new suspendable listener to this event node.
 */
fun <E : Event> EventNode<in E>.addSuspendingListener(
    extension: Extension,
    eventType: Class<E>,
    listener: suspend (E) -> Unit
) {
    this.addListener(eventType) { e ->
        extension.launch {
            listener.invoke(e)
        }
    }
}

/**
 * Adds a new suspendable handler to this builder.
 */
fun <E : Event> EventListener.Builder<E>.suspendingHandler(
    server: MinecraftServer,
    listener: suspend (E) -> Unit
): EventListener.Builder<E> {
    return this.handler { e ->
        server.launch {
            listener.invoke(e)
        }
    }
}

/**
 * Adds a new suspendable handler to this builder.
 */
fun <E : Event> EventListener.Builder<E>.suspendingHandler(
    extension: Extension,
    listener: suspend (E) -> Unit
): EventListener.Builder<E> {
    return this.handler { e ->
        extension.launch {
            listener.invoke(e)
        }
    }
}

/**
 * Hidden internal MCCoroutine interface.
 */
interface MCCoroutine {
    companion object {
        /**
         * Allows to change the driver to load different kinds of MCCoroutine implementations.
         * e.g. loading the implementation for UnitTests.
         */
        var Driver: String = "com.github.shynixn.mccoroutine.minestom.impl.MCCoroutineImpl"
    }

    /**
     * Get coroutine session for the given extension.
     * When using an extension, coroutine scope is bound to the lifetime of the extension.
     */
    fun getCoroutineSession(extension: Extension): CoroutineSession

    /**
     * Get coroutine session for the given server.
     * When using a server, coroutine scope is bound to the lifetime of the entire server.
     */
    fun getCoroutineSession(minecraftServer: MinecraftServer): CoroutineSession

    /**
     * Disposes the given extension coroutine session.
     */
    fun disable(extension: Extension)

    /**
     * Disposes the given server coroutine session.
     */
    fun disable(minecraftServer: MinecraftServer)
}
