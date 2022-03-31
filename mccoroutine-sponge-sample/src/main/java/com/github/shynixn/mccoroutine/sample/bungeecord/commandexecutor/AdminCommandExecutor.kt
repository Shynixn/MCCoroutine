package com.github.shynixn.mccoroutine.sample.bungeecord.commandexecutor

import com.github.shynixn.mccoroutine.SuspendingCommandElement
import com.github.shynixn.mccoroutine.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.postSuspending
import com.github.shynixn.mccoroutine.sample.bungeecord.impl.UserDataCache
import kotlinx.coroutines.joinAll
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.ArgumentParseException
import org.spongepowered.api.command.args.CommandArgs
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.impl.AbstractEvent
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.text.Text

class AdminCommandExecutor(private val userDataCache: UserDataCache, private val pluginContainer: PluginContainer) :
    SuspendingCommandExecutor {
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

        println("[AdminCommandExecutor] Is starting on Primary Thread: " + Sponge.getServer().isMainThread)
        val event = MCCoroutineEvent()
        Sponge.getEventManager().postSuspending(event, pluginContainer).joinAll()
        println("[AdminCommandExecutor] Is ending on Primary Thread: " + Sponge.getServer().isMainThread)

        return CommandResult.success()
    }

    class MCCoroutineEvent : AbstractEvent() {
        /**
         * Gets the cause for the event.
         *
         * @return The cause
         */
        override fun getCause(): Cause? {
            return null
        }
    }

    class SetCommandElement(pluginContainer: PluginContainer, text: Text) :
        SuspendingCommandElement(pluginContainer, text) {
        /**
         * Attempt to extract a value for this element from the given arguments.
         * This method is expected to have no side-effects for the source, meaning
         * that executing it will not change the state of the [CommandSource]
         * in any way.
         *
         * @param source The source to parse for
         * @param args the arguments
         * @return The extracted value
         * @throws ArgumentParseException if unable to extract a value
         */
        override suspend fun parseValue(source: CommandSource, args: CommandArgs): Any? {
            val value = args.next()

            if (value.equals("set", true)) {
                return "set"
            }

            args.createError(Text.of("Input $value is not 'set'."))
            return null
        }

        /**
         * Fetch completions for command arguments.
         *
         * @param src The source requesting tab completions
         * @param args The arguments currently provided
         * @param context The context to store state in
         * @return Any relevant completions
         */
        override suspend fun complete(
            src: CommandSource,
            args: CommandArgs,
            context: CommandContext
        ): List<String?>? {
            return listOf("set")
        }
    }
}
