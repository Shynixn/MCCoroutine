package com.github.shynixn.mccoroutine.sample

import com.github.shynixn.mccoroutine.launchMinecraft
import com.github.shynixn.mccoroutine.registerSuspendingEventFlow
import com.github.shynixn.mccoroutine.registerSuspendingEvents
import kotlinx.coroutines.flow.collect
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

class MCCoroutineSamplePlugin : JavaPlugin() {
    /**
     * OnEnable.
     */
    override fun onEnable() {
        super.onEnable()

        val plugin = this
        launchMinecraft {

            plugin.server.pluginManager
                .registerSuspendingEventFlow(PlayerJoinEvent::class.java, plugin)
                .collect {
                    it.eventName
                }
        }


    }
}
