package com.github.shynixn.mccoroutine.entity

import com.github.shynixn.mccoroutine.CommandEvent
import com.github.shynixn.mccoroutine.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.contract.CommandService
import com.github.shynixn.mccoroutine.contract.CoroutineSession
import com.github.shynixn.mccoroutine.launchMinecraft
import com.github.shynixn.mccoroutine.minecraftDispatcher
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredListener
import java.util.*

class CommandServiceImpl(private val plugin: Plugin, private val coroutineSession: CoroutineSession) : CommandService {
    /**
     * Registers a suspend command executor.
     */
    override fun registerSuspendCommandExecutor(
        pluginCommand: PluginCommand,
        commandExecutor: SuspendingCommandExecutor
    ) {
        pluginCommand.setExecutor { p0, p1, p2, p3 ->
            // If the result is delayed we can automatically assume it is true.
            var success = true

            plugin.launchMinecraft {
                success = commandExecutor.onCommand(p0, p1, p2, p3)
            }

            success
        }
    }

    /**
     * Creates a new command flow.
     */
    override fun createCommandFlow(pluginCommand: PluginCommand): Flow<CommandEvent> {
        val uuid = UUID.randomUUID()
        val executor = CommandExecutor { p0, p1, p2, p3 ->
            val command = CommandEventImpl(p0, p1, p3 as Array<String>, p2)
            coroutineSession.flows[uuid]!!.channel.offer(command)
            command.success
        }
        pluginCommand.setExecutor(executor)

        return channelFlow<CommandEvent> {
            coroutineSession.flows[uuid] = this as ProducerScope<Any>
            awaitClose {}
        }.flowOn(plugin.minecraftDispatcher)
    }
}
