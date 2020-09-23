package com.github.shynixn.mccoroutine.sample

import com.github.shynixn.mccoroutine.*
import com.github.shynixn.mccoroutine.sample.commandexecutor.AdminCommandExecutor
import com.github.shynixn.mccoroutine.sample.flow.AdminCommandFlow
import com.github.shynixn.mccoroutine.sample.flow.PlayerConnectFlow
import com.github.shynixn.mccoroutine.sample.impl.FakeDatabase
import com.github.shynixn.mccoroutine.sample.impl.UserDataCache
import com.github.shynixn.mccoroutine.sample.listener.PlayerConnectListener
import kotlinx.coroutines.flow.collect
import org.bukkit.command.PluginCommand
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

class MCCoroutineSamplePlugin : JavaPlugin() {
    /**
     * OnEnable.
     */
    override fun onEnable() {
        super.onEnable()
        val database = FakeDatabase()
        val cache = UserDataCache(this, database)

        // Extension to traditional registration.
        server.pluginManager.registerSuspendingEvents(PlayerConnectListener(this, cache), this)
        this.getCommand("mccor")!!.setSuspendingExecutor(AdminCommandExecutor(cache))

        // Most of the time you want to go with the options above. Below are only special cases
        // for approaches.
        val plugin = this
        val playerConnectFlow = PlayerConnectFlow(this, cache)
        val adminCommandFlow = AdminCommandFlow(cache)
        launchMinecraft {
            server.pluginManager.registerSuspendingEventFlow(PlayerJoinEvent::class, plugin)
                .collect {
                    playerConnectFlow.onPlayerJoinEvent(it)
                }
        }
        launchMinecraft {
            server.pluginManager.registerSuspendingEventFlow(PlayerQuitEvent::class, plugin)
                .collect {
                    playerConnectFlow.onPlayerQuitEvent(it)
                }
        }
        launchMinecraft {
            plugin.getCommand("mccorflow")!!.registerSuspendingCommandFlow()
                .collect {
                    adminCommandFlow.onCommand(it)
                }
        }
    }
}
