# Suspending Commandexecutors

This page explains how you can use Kotlin Coroutines using the suspend key word for command executors in minecraft
plugins.

## Create the CommandExecutor

=== "Bukkit"

    Create a traditional command executor but implement ``SuspendingCommandExecutor`` instead of ``CommandExecutor``. Please
    consider, that the return value ``true`` is automatically assumed, if the function is suspended in one branch.
    
    ````kotlin
    import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
    import org.bukkit.command.Command
    import org.bukkit.command.CommandSender
    import org.bukkit.entity.Player
    
    class PlayerDataCommandExecutor(private val database: Database) : SuspendingCommandExecutor {
        override suspend fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
            if (sender !is Player) {
                return false
            }
    
            if (args.size == 2 && args[0].equals("rename", true)) {
                val name = args[1]
                val playerData = database.getDataFromPlayer(sender)
                playerData.name = name
                database.saveData(sender, playerData)
                return true
            }
    
            return false
        }
    }
    ````

=== "BungeeCord"

    Create a traditional command executor but extend from ``SuspendingCommand`` instead of ``Command``.

    ````kotlin
    import com.github.shynixn.mccoroutine.bungeecord.SuspendingCommand
    import net.md_5.bungee.api.CommandSender
    import net.md_5.bungee.api.connection.ProxiedPlayer
    
    class PlayerDataCommandExecutor(private val database: Database) : SuspendingCommand("playerdata") {
        override suspend fun execute(sender: CommandSender, args: Array<out String>) {
            if (sender !is ProxiedPlayer) {
                return
            }
    
            if (args.size == 2 && args[0].equals("rename", true)) {
                val name = args[1]
                val playerData = database.getDataFromPlayer(sender)
                playerData.name = name
                database.saveData(sender, playerData)
                return
            }
        }
    }
    ````

=== "Fabric"

    Create a traditional command executor but extend from ``SuspendingCommand`` instead of ``SuspendingCommand``.
    
    ````kotlin
    import com.github.shynixn.mccoroutine.fabric.SuspendingCommand
    import com.mojang.brigadier.context.CommandContext
    import net.minecraft.entity.player.PlayerEntity
    import net.minecraft.server.command.ServerCommandSource

    class PlayerDataCommandExecutor : SuspendingCommand<ServerCommandSource> {
        override suspend fun run(context: CommandContext<ServerCommandSource>): Int {
            if (context.source.entity is PlayerEntity) {
                val sender = context.source.entity as PlayerEntity
                println("[PlayerDataCommandExecutor] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}")
            }
    
            return 1
        }
    }
    ````

=== "Folia"

    Folia schedules threads on the region of the entity who executed this command. For the console (globalregion) and command blocks (region) this rule
    applies as well. Other than that, usage is almost identical to Bukkit.
    
    ````kotlin
    import com.github.shynixn.mccoroutine.folia.SuspendingCommandExecutor
    import org.bukkit.command.Command
    import org.bukkit.command.CommandSender
    import org.bukkit.entity.Player
    
    class PlayerDataCommandExecutor(private val database: Database) : SuspendingCommandExecutor {
        override suspend fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
            // In Folia, this will be the global region thread, or entity execution thread.
            // In Bukkit, this will be the main thread.
    
            if (sender !is Player) {
                return false
            }
    
            if (args.size == 2 && args[0].equals("rename", true)) {
                val name = args[1]
                withContext(plugin.mainDispatcher) {
                    // Make sure you switch to your plugin main thread before you do anything in your plugin.
                    val playerData = database.getDataFromPlayer(sender)
                    playerData.name = name
                    database.saveData(sender, playerData)
                }

                return true
            }
    
            return false
        }
    }
    ````

=== "Minestom"

    Create a traditional command and user ``server.launch`` or ``extension.launch`` in the addSyntax handler.
    
    ````kotlin
    import com.github.shynixn.mccoroutine.minestom.launch
    import net.minestom.server.MinecraftServer
    import net.minestom.server.command.builder.Command
    import net.minestom.server.command.builder.arguments.ArgumentType
    import net.minestom.server.entity.Player
    
    class PlayerDataCommandExecutor(private val server: MinecraftServer, private val database: Database) : Command("mycommand") {
        init {
            val nameArgument = ArgumentType.String("name")
            addSyntax({ sender, context ->
                server.launch {
                    if (sender is Player) {
                        val name : String = context.get(nameArgument)
                        val playerData = database.getDataFromPlayer(sender)
                        playerData.name = name
                        database.saveData(sender, playerData)
                    }
                }
            })
        }
    }
    ````

=== "Sponge"

    Create a traditional command executor but extend from ``SuspendingCommandExecutor`` instead of ``CommandExecutor``. Please
    consider, that the return value ``CommandResult.success()`` is automatically assumed, if the function is suspended in one branch.

    ````kotlin
    import com.github.shynixn.mccoroutine.sponge.SuspendingCommandExecutor
    import org.spongepowered.api.command.CommandResult
    import org.spongepowered.api.command.CommandSource
    import org.spongepowered.api.command.args.CommandContext
    import org.spongepowered.api.entity.living.player.Player
    
    class PlayerDataCommandExecutor(private val database: Database) : SuspendingCommandExecutor {
        override suspend fun execute(src: CommandSource, args: CommandContext): CommandResult {
            if (src !is Player) {
                return CommandResult.empty()
            }
    
            if (args.hasAny("name")) {
                val name = args.getOne<String>("name").get()
                val playerData = database.getDataFromPlayer(src)
                playerData.name = name
                database.saveData(src, playerData)
                return CommandResult.success()
            }
    
            return CommandResult.empty()
        }
    }
    ````

=== "Velocity"

    There are multiple ways to create command executors in Velocity. MCCoroutine provides extensions for both the ``SimpleCommand`` and
    the ``BrigadierCommand`` to allow flexibility. 

    A ``SimpleCommand`` can  be created by implementing ``SuspendingSimpleCommand`` instead of ``SimpleCommand``

    ````kotlin
    import com.github.shynixn.mccoroutine.velocity.SuspendingSimpleCommand
    import com.velocitypowered.api.command.SimpleCommand
    import com.velocitypowered.api.proxy.Player
    
    class PlayerDataCommandExecutor(private val database: Database) : SuspendingSimpleCommand {
        override suspend fun execute(invocation: SimpleCommand.Invocation) {
            val source = invocation.source()
    
            if (source !is Player) {
                return
            }
    
            val args = invocation.arguments()
    
            if (args.size == 2 && args[0].equals("rename", true)) {
                val name = args[1]
                val playerData = database.getDataFromPlayer(source)
                playerData.name = name
                database.saveData(source, playerData)
                return
            }
        }
    }
    ````

    A ``BrigadierCommand`` can be executed asynchronously using the ``executesSuspend`` extension function. More details below.

## Register the CommandExecutor

=== "Bukkit"

    Instead of using ``setExecutor``, use the provided extension method ``setSuspendingExecutor`` to register a command executor.
    
    !!! note "Important"
        Do not forget to declare the ``playerdata`` command in your plugin.yml.
    
    ````kotlin
    import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
    import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
    import com.github.shynixn.mccoroutine.bukkit.setSuspendingExecutor
    
    class MCCoroutineSamplePlugin : SuspendingJavaPlugin() {
        private val database = Database()
    
        override suspend fun onEnableAsync() {
            // Minecraft Main Thread
            database.createDbIfNotExist()
            server.pluginManager.registerSuspendingEvents(PlayerDataListener(database), this)
            getCommand("playerdata")!!.setSuspendingExecutor(PlayerDataCommandExecutor(database))
        }
    
        override suspend fun onDisableAsync() {
            // Minecraft Main Thread
        }
    }
    ````

=== "BungeeCord"

    Instead of using ``registerCommand``, use the provided extension method ``registerSuspendingCommand`` to register a command executor.

    !!! note "Important"
        Do not forget to declare the ``playerdata`` command in your plugin.yml.

    ````kotlin
    import com.github.shynixn.mccoroutine.bungeecord.SuspendingPlugin
    import com.github.shynixn.mccoroutine.bungeecord.registerSuspendingCommand
    import com.github.shynixn.mccoroutine.bungeecord.registerSuspendingListener
    
    class MCCoroutineSamplePlugin : SuspendingPlugin() {
        private val database = Database()
    
        override suspend fun onEnableAsync() {
            // BungeeCord Startup Thread
            database.createDbIfNotExist()
            proxy.pluginManager.registerSuspendingListener(this, PlayerDataListener(database))
            proxy.pluginManager.registerSuspendingCommand(this, PlayerDataCommandExecutor(database))
        }
    
        override suspend fun onDisableAsync() {
            // BungeeCord Shutdown Thread (Not the same as the startup thread)
        }
    }
    ````

=== "Fabric"

    ````kotlin
    class MCCoroutineSampleServerMod : DedicatedServerModInitializer {
        override fun onInitializeServer() {
            ServerLifecycleEvents.SERVER_STARTING.register(ServerLifecycleEvents.ServerStarting { server ->
                // Connect Native Minecraft Scheduler and MCCoroutine.
                mcCoroutineConfiguration.minecraftExecutor = Executor { r ->
                    server.submitAndJoin(r)
                }
                launch {
                    onServerStarting(server)
                }
            })
    
            ServerLifecycleEvents.SERVER_STOPPING.register { server ->
                mcCoroutineConfiguration.disposePluginSession()
            }
        }

        /**
         * MCCoroutine is ready after the server has started.
         */
        private suspend fun onServerStarting(server : MinecraftServer) {
            // Register command
            val command = PlayerDataCommandExecutor()
            server.commandManager.dispatcher.register(CommandManager.literal("mccor").executesSuspend(this, command))
        }
    }
    ````

=== "Folia"

    Instead of using ``setExecutor``, use the provided extension method ``setSuspendingExecutor`` to register a command executor.
    
    !!! note "Important"
        Do not forget to declare the ``playerdata`` command in your plugin.yml.
    
    ````kotlin
    import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
    import com.github.shynixn.mccoroutine.folia.registerSuspendingEvents
    import com.github.shynixn.mccoroutine.folia.setSuspendingExecutor
    
    class MCCoroutineSamplePlugin : SuspendingJavaPlugin() {
        private val database = Database()
    
        override suspend fun onEnableAsync() {
            // Global Region Thread.
            database.createDbIfNotExist()
            server.pluginManager.registerSuspendingEvents(PlayerDataListener(database), this)
            getCommand("playerdata")!!.setSuspendingExecutor(PlayerDataCommandExecutor(database))
        }
    
        override suspend fun onDisableAsync() {
             // Global Region Thread.
        }
    }
    ````

=== "Minestom"

    Register the command in the same way as a traditional command.

=== "Sponge"

    Instead of using ``executor``, use the provided extension method ``suspendingExecutor`` to register a command executor.

    ````kotlin
    import com.github.shynixn.mccoroutine.sponge.SuspendingPluginContainer
    import com.github.shynixn.mccoroutine.sponge.registerSuspendingListeners
    import com.github.shynixn.mccoroutine.sponge.suspendingExecutor
    import com.google.inject.Inject
    import org.spongepowered.api.Sponge
    import org.spongepowered.api.command.args.GenericArguments
    import org.spongepowered.api.command.spec.CommandSpec
    import org.spongepowered.api.event.Listener
    import org.spongepowered.api.event.game.state.GameStartedServerEvent
    import org.spongepowered.api.event.game.state.GameStoppingServerEvent
    import org.spongepowered.api.plugin.Plugin
    import org.spongepowered.api.plugin.PluginContainer
    import org.spongepowered.api.text.Text
    
    @Plugin(
        id = "mccoroutinesample",
        name = "MCCoroutineSample",
        description = "MCCoroutineSample is sample plugin to use MCCoroutine in Sponge."
    )
    class MCCoroutineSamplePlugin {
        private val database = Database()
    
        @Inject
        private lateinit var suspendingPluginContainer: SuspendingPluginContainer
    
        @Inject
        private lateinit var pluginContainer: PluginContainer
    
        @Listener
        suspend fun onEnable(event: GameStartedServerEvent) {
            // Minecraft Main Thread
            database.createDbIfNotExist()
            Sponge.getEventManager().registerSuspendingListeners(pluginContainer, PlayerDataListener(database))
            val commandSpec = CommandSpec.builder()
                .description(Text.of("Command for operations."))
                .permission("mccoroutine.sample")
                .arguments(
                    GenericArguments.onlyOne(GenericArguments.string(Text.of("name")))
                )
                .suspendingExecutor(pluginContainer, PlayerDataCommandExecutor(database))
            Sponge.getCommandManager().register(pluginContainer, commandSpec.build(), listOf("playerdata"))
        }
    
        @Listener
        suspend fun onDisable(event: GameStoppingServerEvent) {
            // Minecraft Main Thread
        }
    }
    ````

=== "Velocity"

    Instead of using ``register``, use the provided extension method ``registerSuspend`` to register a simple command executor.

    ````kotlin
    @Plugin(
        id = "mccoroutinesample",
        name = "MCCoroutineSample",
        description = "MCCoroutineSample is sample plugin to use MCCoroutine in Velocity."
    )
    class MCCoroutineSamplePlugin {
        private val database = Database()
    
        @Inject
        lateinit var proxyServer: ProxyServer
    
        @Inject
        constructor(suspendingPluginContainer: SuspendingPluginContainer) {
            suspendingPluginContainer.initialize(this)
        }
    
        @Subscribe
        suspend fun onProxyInitialization(event: ProxyInitializeEvent) {
            // Velocity Thread Pool
            database.createDbIfNotExist()
            proxyServer.eventManager.registerSuspend(this, PlayerDataListener(database))
            val meta = proxyServer.commandManager.metaBuilder("playerdata").build()
    
            // Register SimpleCommand
            proxyServer.commandManager.registerSuspend(meta, PlayerDataCommandExecutor(database), this)
    
            // Register BrigadierCommand
            val helloCommand =
                LiteralArgumentBuilder.literal<CommandSource>("test")
                    .executesSuspend(this, { context: CommandContext<CommandSource> ->
                        val message = Component.text("Hello World", NamedTextColor.AQUA)
                        context.getSource().sendMessage(message)
                        1 // indicates success
                    })
                    .build()
            proxyServer.commandManager.register(BrigadierCommand(helloCommand))
        }
    }
    ````

## Test the CommandExecutor

Join your server and use the playerData command to observe ``getDataFromPlayer`` and ``saveData`` messages getting
printed to your server log.
