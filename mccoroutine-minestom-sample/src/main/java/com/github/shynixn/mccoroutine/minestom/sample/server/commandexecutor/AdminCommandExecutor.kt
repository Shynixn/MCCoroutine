package com.github.shynixn.mccoroutine.minestom.sample.server.commandexecutor

import com.github.shynixn.mccoroutine.minestom.launch
import com.github.shynixn.mccoroutine.minestom.sample.server.impl.UserDataCache
import com.github.shynixn.mccoroutine.minestom.setSuspendingDefaultExecutor
import kotlinx.coroutines.delay
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

class AdminCommandExecutor(private val userDataCache: UserDataCache, private val server: MinecraftServer) :
    Command("mccor") {
    init {
        setSuspendingDefaultExecutor(server) { sender, context ->
            println("Say hello in 1 second")
            delay(1000L)
            sender.sendMessage("/mccor set <player> <kill>")
            sender.sendMessage("/mccor leave")
            sender.sendMessage("/mccor exception")
        }

        val setArgument = ArgumentType.String("set")
        val killsArgument = ArgumentType.Integer("kills");

        addSyntax({ sender, context ->
            server.launch {
                if (sender is Player) {
                    val set = context.get(setArgument)
                    val kills: Int = context.get(killsArgument)
                    println("[AdmingCommandExecutor/onCommand] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
                    val userData = userDataCache.getUserDataFromPlayerAsync(sender).await()
                    userData.amountOfPlayerKills = kills
                    userDataCache.saveUserData(sender)
                    println("[AdmingCommandExecutor/onCommand] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
                    sender.sendMessage("Done!")
                }
            }
        })
    }
}
