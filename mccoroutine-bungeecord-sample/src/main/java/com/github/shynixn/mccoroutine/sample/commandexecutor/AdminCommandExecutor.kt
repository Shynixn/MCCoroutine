package com.github.shynixn.mccoroutine.sample.commandexecutor

import com.github.shynixn.mccoroutine.bungeecord.SuspendingCommand
import com.github.shynixn.mccoroutine.sample.impl.UserDataCache
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Plugin

class AdminCommandExecutor(private val userDataCache: UserDataCache, private val plugin: Plugin) :
    SuspendingCommand("mccor") {

    override suspend fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.size == 3 && args[0].equals("set", true) && args[2].toIntOrNull() != null) {
            val playerName = args[1]
            val playerKills = args[2].toInt()
            val otherPlayer = plugin.proxy.getPlayer(playerName)!!

            println("[AdminCommandExecutor] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            val userData = userDataCache.getUserDataFromPlayerAsync(otherPlayer).await()
            userData.amountOfPlayerKills = playerKills
            userDataCache.saveUserData(otherPlayer)
            println("[AdminCommandExecutor] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            return
        }

        if (args.isEmpty()) {
            sender.sendMessage("/mccor set <player> <kill>")
            sender.sendMessage("/mccor leave")
            return
        }
    }
}
