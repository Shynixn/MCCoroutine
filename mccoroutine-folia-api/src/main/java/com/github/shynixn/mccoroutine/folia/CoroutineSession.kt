package com.github.shynixn.mccoroutine.folia

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.bukkit.World
import org.bukkit.command.PluginCommand
import org.bukkit.entity.Entity
import org.bukkit.event.Event
import org.bukkit.event.Listener
import kotlin.coroutines.CoroutineContext

/**
 * Facade of a coroutine session of a single plugin.
 */
interface CoroutineSession {
    /**
     * Plugin scope.
     */
    val scope: CoroutineScope

    /**
     * The global region dispatcher is simply used to perform edits on data that the global region owns, such as game rules, day time, weather, or to execute commands using the console command sender.
     */
    val dispatcherGlobalRegion: CoroutineContext

    /**
     * All async operations dispatcher.
     */
    val dispatcherAsync: CoroutineContext

    /**
     * The main dispatcher represents the main thread of a plugin.
     */
    val dispatcherMain: CoroutineContext

    /**
     * Manipulates the bukkit server heart beat on startup.
     */
    var isManipulatedServerHeartBeatEnabled: Boolean

    /**
     * MCCoroutine Facade.
     */
    val mcCoroutineConfiguration: MCCoroutineConfiguration

    /**
     * The thread id of the dispatcherMain.
     */
    val dispatcherMainThreadId: Long

    /**
     * Gets if the Folia schedulers where successfully loaded into MCCoroutine.
     * Returns false if MCCoroutine falls back to the BukkitScheduler.
     */
    val isFoliaLoaded: Boolean

    /**
     * The RegionizedTaskQueue allows tasks to be scheduled to be executed on the next tick of a region that owns a specific location, or creating such region if it does not exist.
     */
    fun getRegionDispatcher(world: World, chunkX: Int, chunkZ: Int): CoroutineContext

    /**
    The EntityScheduler allows tasks to be scheduled to be executed on the region that owns the entity.
     */
    fun getEntityDispatcher(entity: Entity): CoroutineContext

    /**
     * Registers a suspend command executor.
     */
    fun registerSuspendCommandExecutor(
        context: CoroutineContext,
        pluginCommand: PluginCommand,
        commandExecutor: SuspendingCommandExecutor
    )

    /**
     * Registers a suspend tab completer.
     */
    fun registerSuspendTabCompleter(
        context: CoroutineContext,
        pluginCommand: PluginCommand,
        tabCompleter: SuspendingTabCompleter
    )

    /**
     * Registers a suspend listener.
     */
    fun registerSuspendListener(
        listener: Listener,
        eventDispatcher: Map<Class<out Event>, (event: Event) -> CoroutineContext>
    )

    /**
     * Fires a suspending [event] with the given [eventExecutionType].
     * @return Collection of receiver jobs. May already be completed.
     */
    fun fireSuspendingEvent(
        event: Event,
        eventExecutionType: EventExecutionType
    ): Collection<Job>
}
