package com.github.shynixn.mccoroutine.folia.test.impl

import com.github.shynixn.mccoroutine.folia.*
import com.github.shynixn.mccoroutine.folia.test.dispatcher.TestMainCoroutineDispatcher
import com.github.shynixn.mccoroutine.folia.test.dispatcher.TestMultiThreadsCoroutineDispatcher
import com.github.shynixn.mccoroutine.folia.test.dispatcher.TestSingleThreadsCoroutineDispatcher
import kotlinx.coroutines.*
import org.bukkit.World
import org.bukkit.command.PluginCommand
import org.bukkit.entity.Entity
import org.bukkit.event.Event
import org.bukkit.event.Listener
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext

internal class TestCoroutineSessionImpl(override val mcCoroutineConfiguration: MCCoroutineConfiguration) :
    CoroutineSession {
    /**
     * Gets if the Folia schedulers where successfully loaded into MCCoroutine.
     * Returns false if MCCoroutine falls back to the BukkitScheduler.
     */
    override val isFoliaLoaded: Boolean = true
    private val regionDispatcher = TestMultiThreadsCoroutineDispatcher("[RegionThreadPool]")
    private val entityDispatcher = TestMultiThreadsCoroutineDispatcher("[EntityThreadPool]")

    /**
     * Gets minecraft coroutine scope.
     */
    override val scope: CoroutineScope

    /**
     * The global region dispatcher is simply used to perform edits on data that the global region owns, such as game rules, day time, weather, or to execute commands using the console command sender.
     */
    override val dispatcherGlobalRegion: CoroutineContext by lazy {
        TestSingleThreadsCoroutineDispatcher("[GlobalRegionThread]")
    }

    /**
     * Gets the async dispatcher.
     */
    override val dispatcherAsync: CoroutineContext by lazy {
        TestMultiThreadsCoroutineDispatcher("[AsyncThreadPool]")
    }

    /**
     * The main dispatcher represents the main thread of a plugin.
     */
    override val dispatcherMain: TestMainCoroutineDispatcher by lazy {
        TestMainCoroutineDispatcher()
    }

    /**
     * The RegionizedTaskQueue allows tasks to be scheduled to be executed on the next tick of a region that owns a specific location, or creating such region if it does not exist.
     */
    override fun getRegionDispatcher(world: World, chunkX: Int, chunkZ: Int): CoroutineContext {
        if (isFoliaLoaded) {
            return regionDispatcher
        }

        return dispatcherGlobalRegion // minecraftDispatcher on BukkitOnly servers
    }

    /**
    The EntityScheduler allows tasks to be scheduled to be executed on the region that owns the entity.
     */
    override fun getEntityDispatcher(entity: Entity): CoroutineContext {
        if (isFoliaLoaded) {
            return entityDispatcher
        }

        return dispatcherGlobalRegion // minecraftDispatcher on BukkitOnly servers
    }

    /**
     * Manipulates the bukkit server heart beat on startup.
     */
    override var isManipulatedServerHeartBeatEnabled: Boolean = false

    /**
     * The thread id of the dispatcherMain.
     */
    override val dispatcherMainThreadId: Long
        get() {
            return dispatcherMain.threadId!!
        }

    init {
        // Root Exception Handler. All Exception which are not consumed by the caller end up here.
        val exceptionHandler = CoroutineExceptionHandler { _, e ->
            Logger.getLogger("TestMCCoroutine").log(
                Level.SEVERE,
                "This is not an error of MCCoroutine! See sub exception for details.",
                e
            )
        }

        // Build Coroutine plugin scope for exception handling
        val rootCoroutineScope = CoroutineScope(exceptionHandler)

        // Minecraft Scope is child of plugin scope and super visor job (e.g. children of a supervisor job can fail independently).
        scope = rootCoroutineScope + SupervisorJob() + dispatcherGlobalRegion
    }

    /**
     * Registers a suspend command executor.
     */
    override fun registerSuspendCommandExecutor(
        context: CoroutineContext,
        pluginCommand: PluginCommand,
        commandExecutor: SuspendingCommandExecutor
    ) {
    }

    /**
     * Registers a suspend tab completer.
     */
    override fun registerSuspendTabCompleter(
        context: CoroutineContext,
        pluginCommand: PluginCommand,
        tabCompleter: SuspendingTabCompleter
    ) {
    }

    /**
     * Registers a suspend listener.
     */
    override fun registerSuspendListener(
        listener: Listener,
        eventDispatcher: Map<Class<out Event>, (event: Event) -> CoroutineContext>
    ) {
    }


    /**
     * Fires a suspending [event] with the given [eventExecutionType].
     * @return Collection of receiver jobs. May already be completed.
     */
    override fun fireSuspendingEvent(event: Event, eventExecutionType: EventExecutionType): Collection<Job> {
        return emptyList()
    }

    /**
     * Disposes the session.
     */
    fun dispose() {
        scope.coroutineContext.cancelChildren()
        scope.cancel()
    }
}
