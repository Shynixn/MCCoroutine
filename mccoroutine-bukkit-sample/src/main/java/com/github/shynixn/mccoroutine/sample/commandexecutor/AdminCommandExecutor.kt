package com.github.shynixn.mccoroutine.sample.commandexecutor

import com.github.shynixn.mccoroutine.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.SuspendingTabCompleter
import com.github.shynixn.mccoroutine.callSuspendingEvent
import com.github.shynixn.mccoroutine.sample.impl.UserDataCache
import kotlinx.coroutines.joinAll
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

class AdminCommandExecutor(private val userDataCache: UserDataCache, private val plugin: Plugin) :
    SuspendingCommandExecutor,
    SuspendingTabCompleter {
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
            val userData = userDataCache.getUserDataFromPlayerAsync(otherPlayer).await()
            userData.amountOfPlayerKills = playerKills
            userDataCache.saveUserData(otherPlayer)
            println("[AdminCommandExecutor] Is ending on Primary Thread: " + Bukkit.isPrimaryThread())
            return true
        }

        if (args.size == 1 && args[0].equals("leave", true) && sender is Player) {
            println("[AdminCommandExecutor] Is starting on Primary Thread: " + Bukkit.isPrimaryThread())
            val event = PlayerQuitEvent(sender, null)
            Bukkit.getPluginManager().callSuspendingEvent(event, plugin).joinAll()
            println("[AdminCommandExecutor] Is ending on Primary Thread: " + Bukkit.isPrimaryThread())
        }

        if (args.isEmpty()) {
            sender.sendMessage("/mccor set <player> <kill>")
            sender.sendMessage("/mccor leave")
            return true
        }

        return false
    }

    /**
     * Requests a list of possible completions for a command argument.
     * If the call is suspended during the execution, the returned list will not be shown.
     * @param sender - Source of the command.
     * @param command - Command which was executed.
     * @param alias - Alias of the command which was used.
     * @param args - Passed command arguments.
     * @return A list of possible completions for the final argument, or an empty list.
     */
    override suspend fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        if (args.size == 1) {
            return arrayListOf("set")
        }

        return emptyList()
    }
}
