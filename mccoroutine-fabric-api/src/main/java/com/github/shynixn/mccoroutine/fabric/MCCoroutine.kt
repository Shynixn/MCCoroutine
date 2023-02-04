package com.github.shynixn.mccoroutine.fabric

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import kotlinx.coroutines.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.api.ModInitializer
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
            "Failed to load MCCoroutine implementation. Shade mccoroutine-fabric-core into your application.",
            e
        )
    }
}

/**
 * Gets the configuration instance of MCCoroutine.
 */
val DedicatedServerModInitializer.mcCoroutineConfiguration: MCCoroutineConfiguration
    get() {
        return mcCoroutine.getCoroutineSession(this).mcCoroutineConfiguration
    }

/**
 * Gets the configuration instance of MCCoroutine.
 */
val ClientModInitializer.mcCoroutineConfiguration: MCCoroutineConfiguration
    get() {
        return mcCoroutine.getCoroutineSession(this).mcCoroutineConfiguration
    }

/**
 * Gets the configuration instance of MCCoroutine.
 */
val ModInitializer.mcCoroutineConfiguration: MCCoroutineConfiguration
    get() {
        return mcCoroutine.getCoroutineSession(this).mcCoroutineConfiguration
    }

/**
 * Gets the extension minecraft dispatcher.
 */
val DedicatedServerModInitializer.minecraftDispatcher: CoroutineContext
    get() {
        return mcCoroutine.getCoroutineSession(this).dispatcherMinecraft
    }

/**
 * Gets the extension minecraft dispatcher.
 */
val ClientModInitializer.minecraftDispatcher: CoroutineContext
    get() {
        return mcCoroutine.getCoroutineSession(this).dispatcherMinecraft
    }

/**
 * Gets the extension minecraft dispatcher.
 */
val ModInitializer.minecraftDispatcher: CoroutineContext
    get() {
        return mcCoroutine.getCoroutineSession(this).dispatcherMinecraft
    }

/**
 * Gets the coroutine scope.
 */
val DedicatedServerModInitializer.scope: CoroutineScope
    get() {
        return mcCoroutine.getCoroutineSession(this).scope
    }

/**
 * Gets the coroutine scope.
 */
val ClientModInitializer.scope: CoroutineScope
    get() {
        return mcCoroutine.getCoroutineSession(this).scope
    }

/**
 * Gets the coroutine scope.
 */
val ModInitializer.scope: CoroutineScope
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
 * By default, the coroutine is immediately scheduled for execution if the current thread is already the minecraft thread.
 * If the current thread is not the minecraft thread, the coroutine is moved to the main scheduler and executed
 * in the next tick schedule.
 * Other start options can be specified via `start` parameter. See [CoroutineStart] for details.
 * An optional [start] parameter can be set to [CoroutineStart.LAZY] to start coroutine _lazily_. In this case,
 * the coroutine [Job] is created in _new_ state. It can be explicitly started with [start][Job.start] function
 * and will be started implicitly on the first invocation of [join][Job.join].
 *
 * Uncaught exceptions in this coroutine do not cancel the parent job or any other child jobs. All uncaught exceptions
 * are logged to the MCCoroutine logger by default.
 *
 * @param context The coroutine context to start. Should almost be always be [minecraftDispatcher]. Async operations should be
 * be created using [withContext] after using the default parameters of this method.
 * @param start coroutine start option. The default value is [CoroutineStart.DEFAULT].
 * @param block the coroutine code which will be invoked in the context of the provided scope.
 **/
fun DedicatedServerModInitializer.launch(
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
 * By default, the coroutine is immediately scheduled for execution if the current thread is already the minecraft thread.
 * If the current thread is not the minecraft thread, the coroutine is moved to the main scheduler and executed
 * in the next tick schedule.
 * Other start options can be specified via `start` parameter. See [CoroutineStart] for details.
 * An optional [start] parameter can be set to [CoroutineStart.LAZY] to start coroutine _lazily_. In this case,
 * the coroutine [Job] is created in _new_ state. It can be explicitly started with [start][Job.start] function
 * and will be started implicitly on the first invocation of [join][Job.join].
 *
 * Uncaught exceptions in this coroutine do not cancel the parent job or any other child jobs. All uncaught exceptions
 * are logged to the MCCoroutine logger by default.
 *
 * @param context The coroutine context to start. Should almost be always be [minecraftDispatcher]. Async operations should be
 * be created using [withContext] after using the default parameters of this method.
 * @param start coroutine start option. The default value is [CoroutineStart.DEFAULT].
 * @param block the coroutine code which will be invoked in the context of the provided scope.
 **/
fun ClientModInitializer.launch(
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
 * By default, the coroutine is immediately scheduled for execution if the current thread is already the minecraft thread.
 * If the current thread is not the minecraft thread, the coroutine is moved to the main scheduler and executed
 * in the next tick schedule.
 * Other start options can be specified via `start` parameter. See [CoroutineStart] for details.
 * An optional [start] parameter can be set to [CoroutineStart.LAZY] to start coroutine _lazily_. In this case,
 * the coroutine [Job] is created in _new_ state. It can be explicitly started with [start][Job.start] function
 * and will be started implicitly on the first invocation of [join][Job.join].
 *
 * Uncaught exceptions in this coroutine do not cancel the parent job or any other child jobs. All uncaught exceptions
 * are logged to the MCCoroutine logger by default.
 *
 * @param context The coroutine context to start. Should almost be always be [minecraftDispatcher]. Async operations should be
 * be created using [withContext] after using the default parameters of this method.
 * @param start coroutine start option. The default value is [CoroutineStart.DEFAULT].
 * @param block the coroutine code which will be invoked in the context of the provided scope.
 **/
fun ModInitializer.launch(
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
 * Works in the same way as executes but offers the possibility for suspension.
 */
fun <S, T : ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.executesSuspend(
    owner: DedicatedServerModInitializer,
    command: SuspendingCommand<S>
): T {
    return this.executes { context ->
        var success = Command.SINGLE_SUCCESS

        owner.launch {
            success = command.run(context)
        }

        success
    }
}

/**
 * Works in the same way as executes but offers the possibility for suspension.
 */
fun <S, T : ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.executesSuspend(
    owner: ClientModInitializer,
    command: SuspendingCommand<S>
): T {
    return this.executes { context ->
        var success = Command.SINGLE_SUCCESS

        owner.launch {
            success = command.run(context)
        }

        success
    }
}

/**
 * Works in the same way as executes but offers the possibility for suspension.
 */
fun <S, T : ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.executesSuspend(
    owner: ModInitializer,
    command: SuspendingCommand<S>
): T {
    return this.executes { context ->
        var success = Command.SINGLE_SUCCESS

        owner.launch {
            success = command.run(context)
        }

        success
    }
}

/**
 * Converts the number to ticks for being used together with delay(..).
 * E.g. delay(1.ticks).
 * Minecraft ticks 20 times per second, which means a tick appears every 50 milliseconds. However,
 * delay() does not directly work with the MinecraftScheduler and needs millisecond manipulation to
 * work as expected. Therefore, 1 tick does not equal 50 milliseconds when using this method standalone and only
 * sums up to 50 milliseconds if you use it together with delay.
 */
val Int.ticks: Long
    get() {
        return (this * 50L - 25)
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
        var Driver: String = "com.github.shynixn.mccoroutine.fabric.impl.MCCoroutineImpl"
    }

    /**
     * Get coroutine session for the given mod.
     * When using an extension, coroutine scope is bound to the lifetime of the extension.
     */
    fun getCoroutineSession(handler: Any): CoroutineSession

    /**
     * Disposes the given coroutine session.
     */
    fun disable(handler: Any)
}
