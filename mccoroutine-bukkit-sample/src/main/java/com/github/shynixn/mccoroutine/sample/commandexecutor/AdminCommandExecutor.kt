package com.github.shynixn.mccoroutine.sample.commandexecutor

import com.github.shynixn.mccoroutine.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.sample.impl.UserDataCache
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class AdminCommandExecutor(private val userDataCache: UserDataCache) : SuspendingCommandExecutor {
    /**
     * Executes the given command, returning its success.
     * If false is returned, then the "usage" plugin.yml entry for this command (if defined) will be sent to the player.
     */
    override suspend fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (args.size == 3 && args[0].equals("set", true) && args[2].toIntOrNull() != null) {
            val playerName = args[1]
            val playerKills = args[2].toInt()
            val otherPlayer = Bukkit.getPlayer(playerName)!!

            println("[AdminCommandExecutor] Is starting on Primary Thread: " + Bukkit.isPrimaryThread())
            val userData = userDataCache.getUserDataFromPlayer(otherPlayer)
            userData.amountOfPlayerKills = playerKills
            userDataCache.saveUserData(otherPlayer)
            println("[AdminCommandExecutor] Is ending on Primary Thread: " + Bukkit.isPrimaryThread())
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("/mccor set <player> <kill>")
            return true
        }

        return false
    }
}
