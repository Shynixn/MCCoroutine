package com.github.shynixn.mccoroutine.sample.commandexecutor

import com.github.shynixn.mccoroutine.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.sample.impl.UserDataCache
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player

class AdminCommandExecutor(private val userDataCache: UserDataCache) : SuspendingCommandExecutor {
    /**
     * Callback for the execution of a command.
     *
     * @param src The commander who is executing this command
     * @param args The parsed command arguments for this command
     * @return the result of executing this command.
     */
    override suspend fun execute(src: CommandSource, args: CommandContext): CommandResult {
        val player = args.getOne<Player>("player").get()
        val playerKills = args.getOne<Int>("kills").get()

        println("[AdminCommandExecutor] Is starting on Primary Thread: " + Sponge.getServer().isMainThread)
        val userData = userDataCache.getUserDataFromPlayerAsync(player).await()
        userData.amountOfPlayerKills = playerKills
        userDataCache.saveUserData(player)
        println("[AdminCommandExecutor] Is ending on Primary Thread: " + Sponge.getServer().isMainThread)

        return CommandResult.success()
    }
}
