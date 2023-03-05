package com.github.shynixn.mccoroutine.fabric.server.command

import com.github.shynixn.mccoroutine.fabric.server.impl.UserDataCache
import com.github.shynixn.mccoroutine.fabric.SuspendingCommand
import com.mojang.brigadier.context.CommandContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.ServerCommandSource

class AdminCommandExecutor(private val userDataCache: UserDataCache) : SuspendingCommand<ServerCommandSource> {
    /**
     *  Gets called when the command is triggered.
     */
    override suspend fun run(context: CommandContext<ServerCommandSource>): Int {
        if (context.source.entity is PlayerEntity) {
            val sender = context.source.entity as PlayerEntity
            println("[AdminCommandExecutor/onCommand] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            val userData = userDataCache.getUserDataFromPlayerAsync(sender).await()
            userData.amountOfPlayerKills = 5
            userDataCache.saveUserData(sender)
            println("[AdminCommandExecutor/onCommand] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
        }

        return 1
    }
}
