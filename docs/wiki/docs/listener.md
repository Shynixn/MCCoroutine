# Adding suspending listeners

This page explains how you can use Kotlin Coroutines using the ``suspend`` key word for listeners in minecraft plugins.

## Create the Listener

Create a listener and add suspend to all functions where you perform suspendable operations (e.g. calling other
suspendable functions). You can mix suspendable and non suspendable functions in listeners.

=== "Bukkit"

    ````kotlin
    import org.bukkit.event.EventHandler
    import org.bukkit.event.Listener
    import org.bukkit.event.player.PlayerJoinEvent
    import org.bukkit.event.player.PlayerQuitEvent
    import java.util.*
    
    class PlayerDataListener(private val database: Database) : Listener {
        @EventHandler
        suspend fun onPlayerJoinEvent(event: PlayerJoinEvent) {
            val player = event.player
            val playerData = database.getDataFromPlayer(player)
            playerData.name = player.name
            playerData.lastJoinDate = Date()
            database.saveData(player, playerData)
        }
    
        @EventHandler
        suspend fun onPlayerQuitEvent(event: PlayerQuitEvent) {
            val player = event.player
            val playerData = database.getDataFromPlayer(player)
            playerData.name = player.name
            playerData.lastQuitDate = Date()
            database.saveData(player, playerData)
        }
    }
    ````

=== "BungeeCord"

    ````kotlin
    import net.md_5.bungee.api.event.PostLoginEvent
    import net.md_5.bungee.api.event.ServerDisconnectEvent
    import net.md_5.bungee.api.plugin.Listener
    import net.md_5.bungee.event.EventHandler
    import java.util.*
    
    class PlayerDataListener(private val database: Database) : Listener {
        @EventHandler
        suspend fun onPlayerJoinEvent(event: PostLoginEvent) {
            val player = event.player
            val playerData = database.getDataFromPlayer(player)
            playerData.name = player.name
            playerData.lastJoinDate = Date()
            database.saveData(player, playerData)
        }
    
        @EventHandler
        suspend fun onPlayerQuitEvent(event: ServerDisconnectEvent) {
            val player = event.player
            val playerData = database.getDataFromPlayer(player)
            playerData.name = player.name
            playerData.lastQuitDate = Date()
            database.saveData(player, playerData)
        }
    }
    ````

=== "Sponge"

    ````kotlin
    import org.spongepowered.api.event.Listener
    import org.spongepowered.api.event.network.ClientConnectionEvent
    import java.util.*
    
    class PlayerDataListener(private val database: Database) {
        @Listener
        suspend fun onPlayerJoinEvent(event: ClientConnectionEvent.Join) {
            val player = event.targetEntity
            val playerData = database.getDataFromPlayer(player)
            playerData.name = player.name
            playerData.lastJoinDate = Date()
            database.saveData(player, playerData)
        }
    
        @Listener
        suspend fun onPlayerQuitEvent(event: ClientConnectionEvent.Disconnect) {
            val player = event.targetEntity
            val playerData = database.getDataFromPlayer(player)
            playerData.name = player.name
            playerData.lastQuitDate = Date()
            database.saveData(player, playerData)
        }
    }
    ````

### Register the Listener 

=== "Bukkit"

    Instead of using ``registerEvents``, use the provided extension method ``registerSuspendingEvents`` to allow
    suspendable functions in your listener. Please notice, that timing measurements are no longer accurate for suspendable functions.

    ````kotlin
    import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
    import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
    
    class MCCoroutineSamplePlugin : SuspendingJavaPlugin() {
        private val database = Database()
    
        override suspend fun onEnableAsync() {
            // Minecraft Main Thread
            database.createDbIfNotExist()
            server.pluginManager.registerSuspendingEvents(PlayerDataListener(database), this)
        }
    
        override suspend fun onDisableAsync() {
            // Minecraft Main Thread
        }
    }
    ````

=== "BungeeCord"

    Instead of using ``registerListener``, use the provided extension method ``registerSuspendingListener`` to allow
    suspendable functions in your listener. Please notice, that timing measurements are no longer accurate for suspendable functions.

    ````kotlin
    import com.github.shynixn.mccoroutine.bungeecord.SuspendingPlugin
    import com.github.shynixn.mccoroutine.bungeecord.registerSuspendingListener
    
    class MCCoroutineSamplePlugin : SuspendingPlugin() {
    private val database = Database()
    
        override suspend fun onEnableAsync() {
            // BungeeCord Startup Thread
            database.createDbIfNotExist()
            proxy.pluginManager.registerSuspendingListener(this, PlayerDataListener(database))
        }
    
        override suspend fun onDisableAsync() {
            // BungeeCord Shutdown Thread (Not the same as the startup thread)
        }
    }
    ````

=== "Sponge"

     Instead of using ``registerListeners``, use the provided extension method ``registerSuspendingListeners`` to allow
    suspendable functions in your listener. Please notice, that timing measurements are no longer accurate for suspendable functions.

    ````kotlin
    import com.github.shynixn.mccoroutine.sponge.SuspendingPluginContainer
    import com.github.shynixn.mccoroutine.sponge.registerSuspendingListeners
    import com.google.inject.Inject
    import org.spongepowered.api.Sponge
    import org.spongepowered.api.event.Listener
    import org.spongepowered.api.event.game.state.GameStartedServerEvent
    import org.spongepowered.api.event.game.state.GameStoppingServerEvent
    import org.spongepowered.api.plugin.Plugin
    import org.spongepowered.api.plugin.PluginContainer

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
        private lateinit var pluginContainer : PluginContainer
    
        @Listener
        suspend fun onEnable(event: GameStartedServerEvent) {
            // Minecraft Main Thread
            database.createDbIfNotExist()
            Sponge.getEventManager().registerSuspendingListeners(pluginContainer, PlayerDataListener(database))
        }
    
        @Listener
        suspend fun onDisable(event: GameStoppingServerEvent) {
            // Minecraft Main Thread
        }
    }
    ````

### Test the Listener

Join and leave your server to observe ``getDataFromPlayer`` and ``saveData`` messages getting printed to your server log.
