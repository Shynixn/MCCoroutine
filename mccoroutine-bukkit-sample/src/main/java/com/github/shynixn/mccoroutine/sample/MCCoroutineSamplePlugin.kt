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
    }
}
