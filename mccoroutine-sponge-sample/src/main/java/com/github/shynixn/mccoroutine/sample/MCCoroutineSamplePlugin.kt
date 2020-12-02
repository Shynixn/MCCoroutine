package com.github.shynixn.mccoroutine.sample

import com.github.shynixn.mccoroutine.registerSuspendingListeners
import com.github.shynixn.mccoroutine.sample.commandexecutor.AdminCommandExecutor
import com.github.shynixn.mccoroutine.sample.impl.FakeDatabase
import com.github.shynixn.mccoroutine.sample.impl.UserDataCache
import com.github.shynixn.mccoroutine.sample.listener.EntityInteractListener
import com.github.shynixn.mccoroutine.sample.listener.PlayerConnectListener
import com.github.shynixn.mccoroutine.suspendingExecutor
import com.google.inject.Inject
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.GenericArguments
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.state.GameStartedServerEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.text.Text

@Plugin(
    id = "mccoroutinesample",
    name = "MCCoroutineSample",
    description = "MCCoroutineSample is sample plugin to use MCCoroutine in Sponge."
)
class MCCoroutineSamplePlugin {
    @Inject
    private lateinit var plugin: PluginContainer

    /**
     * OnEnable.
     */
    @Listener
    fun onEnable(event: GameStartedServerEvent) {
        val database = FakeDatabase()
        val cache = UserDataCache(plugin, database)

        // Extension to traditional registration.
        Sponge.getEventManager().registerSuspendingListeners(plugin, PlayerConnectListener(plugin, cache))
        CommandSpec.builder()
            .description(Text.of("Command for operations."))
            .permission("mccoroutine.sample")
            .arguments(
                GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))),
                GenericArguments.onlyOne(GenericArguments.integer(Text.of("kills")))
            )
            .suspendingExecutor(plugin, AdminCommandExecutor(cache))
    }
}
