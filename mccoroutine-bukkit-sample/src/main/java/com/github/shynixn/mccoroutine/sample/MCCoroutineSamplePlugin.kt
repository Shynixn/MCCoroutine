package com.github.shynixn.mccoroutine.sample

import com.github.shynixn.mccoroutine.registerSuspendingEvents
import com.github.shynixn.mccoroutine.sample.commandexecutor.AdminCommandExecutor
import com.github.shynixn.mccoroutine.sample.impl.FakeDatabase
import com.github.shynixn.mccoroutine.sample.impl.UserDataCache
import com.github.shynixn.mccoroutine.sample.listener.EntityInteractListener
import com.github.shynixn.mccoroutine.sample.listener.PlayerConnectListener
import com.github.shynixn.mccoroutine.setSuspendingExecutor
import com.github.shynixn.mccoroutine.setSuspendingTabCompleter
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
        server.pluginManager.registerSuspendingEvents(EntityInteractListener(cache), this);

        val commandExecutor = AdminCommandExecutor(cache, this)
        this.getCommand("mccor")!!.setSuspendingExecutor(commandExecutor)
        this.getCommand("mccor")!!.setSuspendingTabCompleter(commandExecutor)
    }
}
