package com.github.shynixn.mccoroutine.velocity.commandexecutor

import com.github.shynixn.mccoroutine.velocity.SuspendingSimpleCommand
import com.github.shynixn.mccoroutine.velocity.impl.UserDataCache
import com.google.inject.Inject
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component

class AdminCommandExecutor(private val userDataCache: UserDataCache, private val proxyServer: ProxyServer) : SuspendingSimpleCommand {
    /**
     * Executes the command for the specified invocation.
     *
     * @param invocation the invocation context.
     */
    override suspend fun execute(invocation: SimpleCommand.Invocation) {
        val args = invocation.arguments()
        val sender = invocation.source()

        if (args.size == 3 && args[0].equals("set", true) && args[2].toIntOrNull() != null) {
            val playerName = args[1]
            val playerKills = args[2].toInt()
            val otherPlayer = proxyServer.getPlayer(playerName)!!.get()

            println("[AdminCommandExecutor] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            val userData = userDataCache.getUserDataFromPlayerAsync(otherPlayer).await()
            userData.amountOfPlayerKills = playerKills
            userDataCache.saveUserData(otherPlayer)
            println("[AdminCommandExecutor] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            return
        }

        if (args.isEmpty()) {
            sender.sendMessage(Component.text("/mccor set <player> <kill>"))
            sender.sendMessage(Component.text("/mccor leave"))
            return
        }
    }
}
