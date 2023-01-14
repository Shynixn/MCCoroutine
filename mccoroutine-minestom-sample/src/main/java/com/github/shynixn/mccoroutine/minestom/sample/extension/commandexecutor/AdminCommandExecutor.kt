package com.github.shynixn.mccoroutine.minestom.sample.extension.commandexecutor

import com.github.shynixn.mccoroutine.minestom.launch
import com.github.shynixn.mccoroutine.minestom.sample.extension.impl.UserDataCache
import com.github.shynixn.mccoroutine.minestom.setSuspendingDefaultExecutor
import kotlinx.coroutines.delay
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import net.minestom.server.extensions.Extension

class AdminCommandExecutor(private val userDataCache: UserDataCache, private val extension: Extension) :
    Command("mccor2") {
    init {
        setSuspendingDefaultExecutor(extension) { sender, context ->
            println("Say hello in 1 second")
            delay(1000L)
            sender.sendMessage("/mccor set <player> <kill>")
            sender.sendMessage("/mccor leave")
            sender.sendMessage("/mccor exception")
        }

        val killsArgument = ArgumentType.Integer("kills");

        addSyntax({ sender, context ->
            extension.launch {
                if (sender is Player) {
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
