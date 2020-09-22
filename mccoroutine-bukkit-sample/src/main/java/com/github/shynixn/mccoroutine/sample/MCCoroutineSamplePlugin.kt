package com.github.shynixn.mccoroutine.sample

import com.github.shynixn.mccoroutine.sample.flow.PlayerConnectFlow
import com.github.shynixn.mccoroutine.sample.impl.FakeDatabase
import com.github.shynixn.mccoroutine.sample.impl.UserDataCache
import com.github.shynixn.mccoroutine.sample.listener.PlayerConnectListener
import org.bukkit.plugin.java.JavaPlugin

class MCCoroutineSamplePlugin : JavaPlugin() {
    /**
     * OnEnable.
     */
    override fun onEnable() {
        super.onEnable()
        val database = FakeDatabase()
        val cache = UserDataCache(database)

        PlayerConnectListener(this, cache)
        PlayerConnectFlow(this, cache)
    }
}
