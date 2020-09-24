package com.github.shynixn.mccoroutine.sample

import com.github.shynixn.mccoroutine.findClazz
import com.github.shynixn.mccoroutine.registerPackets
import com.github.shynixn.mccoroutine.registerSuspendingEvents
import com.github.shynixn.mccoroutine.sample.commandexecutor.AdminCommandExecutor
import com.github.shynixn.mccoroutine.sample.impl.FakeDatabase
import com.github.shynixn.mccoroutine.sample.impl.UserDataCache
import com.github.shynixn.mccoroutine.sample.listener.PlayerConnectListener
import com.github.shynixn.mccoroutine.sample.packet.MySupportedPacketType
import com.github.shynixn.mccoroutine.setSuspendingExecutor
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

        // Extension to listen for packets.
        server.pluginManager.registerPackets(
            listOf(findClazz(MySupportedPacketType.PACKETPLAYINPOSITIONLOOK.classPath)),
            this
        )
    }
}
