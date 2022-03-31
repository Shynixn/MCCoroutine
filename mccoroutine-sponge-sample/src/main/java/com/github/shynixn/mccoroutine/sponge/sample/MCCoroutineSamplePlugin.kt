package com.github.shynixn.mccoroutine.sponge.sample

import com.github.shynixn.mccoroutine.sponge.SuspendingPluginContainer
import com.github.shynixn.mccoroutine.sponge.asyncDispatcher
import com.github.shynixn.mccoroutine.sponge.registerSuspendingListeners
import com.github.shynixn.mccoroutine.sponge.sample.commandexecutor.AdminCommandExecutor
import com.github.shynixn.mccoroutine.sponge.sample.impl.FakeDatabase
import com.github.shynixn.mccoroutine.sponge.sample.impl.UserDataCache
import com.github.shynixn.mccoroutine.sponge.sample.listener.EntityInteractListener
import com.github.shynixn.mccoroutine.sponge.sample.listener.PlayerConnectListener
import com.github.shynixn.mccoroutine.sponge.suspendingExecutor
import com.google.inject.Inject
import kotlinx.coroutines.withContext
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.GenericArguments
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.state.GameStartedServerEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.text.Text

@Suppress("unused")
@Plugin(
    id = "mccoroutinesample",
    name = "MCCoroutineSample",
    description = "MCCoroutineSample is sample plugin to use MCCoroutine in Sponge."
)
class MCCoroutineSamplePlugin {
    @Inject
    private lateinit var plugin: PluginContainer

    @Inject
    private lateinit var suspendingPluginContainer: SuspendingPluginContainer

    /**
     * OnEnable.
     */
    @Listener
    suspend fun onEnable(event: GameStartedServerEvent) {
        val database = FakeDatabase()
        val cache = UserDataCache(plugin, database)

        println("[MCCoroutineSamplePlugin] OnEnable on Primary Thread: " + Sponge.getServer().isMainThread)

        withContext(plugin.asyncDispatcher) {
            println("[MCCoroutineSamplePlugin] Loading some data on async Thread: " + Sponge.getServer().isMainThread)
            Thread.sleep(500)
        }

        // Extension to traditional registration.
        Sponge.getEventManager().registerSuspendingListeners(plugin, PlayerConnectListener(plugin, cache))
        Sponge.getEventManager().registerSuspendingListeners(plugin, EntityInteractListener(cache))
        val commandSpec = CommandSpec.builder()
            .description(Text.of("Command for operations."))
            .permission("mccoroutine.sample")
            .arguments(
                GenericArguments.onlyOne(
                    AdminCommandExecutor.SetCommandElement(plugin, Text.of("action")).toCommandElement()
                ),
                GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))),
                GenericArguments.onlyOne(GenericArguments.integer(Text.of("kills")))
            )
            .suspendingExecutor(plugin, AdminCommandExecutor(cache, plugin))
        Sponge.getCommandManager().register(plugin, commandSpec.build(), listOf("mccor"))
    }
}
