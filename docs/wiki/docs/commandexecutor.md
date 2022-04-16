# Adding suspending commandExecutors

This page explains how you can use Kotlin Coroutines using the suspend key word for command executors in minecraft plugins.

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
    
            if (args.hasAny("rename")) {
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
                    GenericArguments.onlyOne(GenericArguments.string(Text.of("player")))
                )
                .suspendingExecutor(pluginContainer, PlayerDataCommandExecutor(database))
            Sponge.getCommandManager().register(pluginContainer, commandSpec.build(), listOf("mccor"))
        }
    
        @Listener
        suspend fun onDisable(event: GameStoppingServerEvent) {
            // Minecraft Main Thread
        }
    }
    ````

## Test the CommandExecutor

Join your server and use the playerData command to observe ``getDataFromPlayer`` and ``saveData`` messages getting printed to your server log.
