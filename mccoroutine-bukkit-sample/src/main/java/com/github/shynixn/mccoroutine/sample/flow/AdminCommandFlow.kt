package com.github.shynixn.mccoroutine.sample.flow

import com.github.shynixn.mccoroutine.CommandEvent
import com.github.shynixn.mccoroutine.sample.impl.UserDataCache
import org.bukkit.Bukkit

class AdminCommandFlow(private val userDataCache: UserDataCache) {
    /**
     * Executes the given command, returning its success.
     * If false is returned, then the "usage" plugin.yml entry for this command (if defined) will be sent to the player.
     */
    suspend fun onCommand(commandEvent: CommandEvent) {
        val args = commandEvent.args
        val sender = commandEvent.commandSender

        if (args.size == 3 && args[0].equals("set", true) && args[2].toIntOrNull() != null) {
            val playerName = args[1]
            val playerKills = args[2].toInt()
            val otherPlayer = Bukkit.getPlayer(playerName)!!

            val userData = userDataCache.getUserDataFromPlayer(otherPlayer)
            userData.amountOfPlayerKills = playerKills
            userDataCache.saveUserData(otherPlayer)
            commandEvent.success = true
        }

        if (args.isEmpty()) {
            sender.sendMessage("/mccor set <player> <kill>")
            commandEvent.success = true
        }
    }
}
