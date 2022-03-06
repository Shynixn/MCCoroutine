package com.github.shynixn.mccoroutine.service

import com.github.shynixn.mccoroutine.SuspendingCommand
import com.github.shynixn.mccoroutine.contract.CommandService
import com.github.shynixn.mccoroutine.contract.CoroutineSession
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.Plugin

class BungeeCordCommandServiceImpl(private val plugin: Plugin, private val coroutineSession: CoroutineSession) : CommandService {
    /**
     * Registers a suspend command executor.
     */
    override fun registerSuspendCommandExecutor(command: SuspendingCommand) {
        val handleCommand = object : Command(command.name, command.permission, *command.aliases) {
            init {
                this.permissionMessage = command.permissionMessage
            }

            override fun execute(sender: CommandSender, args: Array<out String>) {
                coroutineSession.launch(coroutineSession.unconfinedDispatcherBungeeCord) {
                    command.execute(sender, args)
                }
            }
        }

        plugin.proxy.pluginManager.registerCommand(plugin, handleCommand)
    }
}
