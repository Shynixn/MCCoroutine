package com.github.shynixn.mccoroutine.folia.impl

import com.github.shynixn.mccoroutine.folia.*
import com.github.shynixn.mccoroutine.folia.dispatcher.*
import com.github.shynixn.mccoroutine.folia.dispatcher.AsyncFoliaCoroutineDispatcher
import com.github.shynixn.mccoroutine.folia.dispatcher.EntityDispatcher
import com.github.shynixn.mccoroutine.folia.dispatcher.GlobalRegionDispatcher
import com.github.shynixn.mccoroutine.folia.dispatcher.RegionDispatcher
import com.github.shynixn.mccoroutine.folia.service.CommandServiceImpl
import com.github.shynixn.mccoroutine.folia.service.EventServiceImpl
import com.github.shynixn.mccoroutine.folia.service.WakeUpBlockServiceImpl
import kotlinx.coroutines.*
import org.bukkit.World
import org.bukkit.command.PluginCommand
import org.bukkit.entity.Entity
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext

internal class CoroutineSessionImpl(
    private val plugin: Plugin,
    override val mcCoroutineConfiguration: MCCoroutineConfiguration
) :
    CoroutineSession {
    /**
     * Gets if the Folia schedulers where successfully loaded into MCCoroutine.
     * Returns false if MCCoroutine falls back to the BukkitScheduler.
     */
    override val isFoliaLoaded: Boolean by lazy {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    /**
     * Gets the block service during startup.
     */
    private val wakeUpBlockService: WakeUpBlockServiceImpl by lazy {
        WakeUpBlockServiceImpl(plugin)
    }

    /**
     * Gets the event service.
     */
    private val eventService: EventServiceImpl by lazy {
        EventServiceImpl(plugin, this)
    }

    /**
     * Gets the command service.
     */
    private val commandService: CommandServiceImpl by lazy {
        CommandServiceImpl(plugin)
    }

    /**
     * Gets minecraft coroutine scope.
     */
    override val scope: CoroutineScope

    /**
     * The global region dispatcher is simply used to perform edits on data that the global region owns, such as game rules, day time, weather, or to execute commands using the console command sender.
     */
    override val dispatcherGlobalRegion: CoroutineContext by lazy {
        if (isFoliaLoaded) {
            GlobalRegionDispatcher(plugin, wakeUpBlockService)
        } else {
            MinecraftCoroutineDispatcher(plugin, wakeUpBlockService)
        }
    }

    /**
     * Gets the async dispatcher.
     */
    override val dispatcherAsync: CoroutineContext by lazy {
        if (isFoliaLoaded) {
            AsyncFoliaCoroutineDispatcher(plugin, wakeUpBlockService)
        } else {
            AsyncCoroutineDispatcher(plugin, wakeUpBlockService)
        }
    }

    /**
     * The main dispatcher represents the main thread of a plugin.
     */
    override val dispatcherMain: MainDispatcher by lazy {
        MainDispatcher(plugin)
    }

    /**
     * The RegionizedTaskQueue allows tasks to be scheduled to be executed on the next tick of a region that owns a specific location, or creating such region if it does not exist.
     */
    override fun getRegionDispatcher(world: World, chunkX: Int, chunkZ: Int): CoroutineContext {
        if (isFoliaLoaded) {
            return RegionDispatcher(plugin, wakeUpBlockService, world, chunkX, chunkZ)
        }

        return dispatcherGlobalRegion // minecraftDispatcher on BukkitOnly servers
    }

    /**
    The EntityScheduler allows tasks to be scheduled to be executed on the region that owns the entity.
     */
    override fun getEntityDispatcher(entity: Entity): CoroutineContext {
        if (isFoliaLoaded) {
            return EntityDispatcher(plugin, wakeUpBlockService, entity)
        }

        return dispatcherGlobalRegion // minecraftDispatcher on BukkitOnly servers
    }

    /**
     * Manipulates the bukkit server heart beat on startup.
     */
    override var isManipulatedServerHeartBeatEnabled: Boolean
        get() {
            return wakeUpBlockService.isManipulatedServerHeartBeatEnabled
        }
        set(value) {
            wakeUpBlockService.isManipulatedServerHeartBeatEnabled = value
        }

    /**
     * The thread id of the dispatcherMain.
     */
    override val dispatcherMainThreadId: Long
        get() {
            return dispatcherMain.threadId
        }

    init {
        // Root Exception Handler. All Exception which are not consumed by the caller end up here.
        val exceptionHandler = CoroutineExceptionHandler { _, e ->
            val mcCoroutineExceptionEvent = MCCoroutineExceptionEvent(plugin, e)

            if (plugin.isEnabled) {
                plugin.launch(plugin.globalRegionDispatcher, CoroutineStart.DEFAULT) {
                    plugin.server.pluginManager.callEvent(mcCoroutineExceptionEvent)

                    if (!mcCoroutineExceptionEvent.isCancelled) {
                        if (e !is CancellationException) {
                            plugin.logger.log(
                                Level.SEVERE,
                                "This is not an error of MCCoroutine! See sub exception for details.",
                                e
                            )
                        }
                    }
                }
            }
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
        commandService.registerSuspendCommandExecutor(context, pluginCommand, commandExecutor)
    }

    /**
     * Registers a suspend tab completer.
     */
    override fun registerSuspendTabCompleter(
        context: CoroutineContext,
        pluginCommand: PluginCommand,
        tabCompleter: SuspendingTabCompleter
    ) {
        commandService.registerSuspendTabCompleter(context, pluginCommand, tabCompleter)
    }

    /**
     * Registers a suspend listener.
     */
    override fun registerSuspendListener(
        listener: Listener,
        eventDispatcher: Map<Class<out Event>, (event: Event) -> CoroutineContext>
    ) {
        eventService.registerSuspendListener(listener, eventDispatcher)
    }


    /**
     * Fires a suspending [event] with the given [eventExecutionType].
     * @return Collection of receiver jobs. May already be completed.
     */
    override fun fireSuspendingEvent(event: Event, eventExecutionType: EventExecutionType): Collection<Job> {
        return eventService.fireSuspendingEvent(event, eventExecutionType)
    }

    /**
     * Disposes the session.
     */
    fun dispose() {
        scope.coroutineContext.cancelChildren()
        scope.cancel()
        dispatcherMain.close()
        wakeUpBlockService.dispose()
    }
}
