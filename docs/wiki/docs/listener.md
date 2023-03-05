# Suspending Listeners

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

    In BungeeCord some events can be [handled asynchronously](https://www.spigotmc.org/wiki/common-development-pitfalls-bungeecord/#blocking-the-i-o-threads). This allows full
    control over consuming, processing and resuming events when performing long running operations. When you create a suspend
    function using MCCoroutine, they automatically handle ``registerIntent`` and ``completeIntent``. You do not have to do anything yourself,
    all suspend functions are automatically processed asynchronously.

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

=== "Velocity"

    In Velocity events can be [handled asynchronously](https://velocitypowered.com/wiki/developers/event-api/). This allows full
    control over consuming, processing and resuming events when performing long running operations. When you create a suspend
    function using MCCoroutine, they automatically handle ``Continuation`` and ``EventTask``. You do not have to do anything yourself,
    all suspend functions are automatically processed asynchronously.

    ````kotlin
    import com.velocitypowered.api.event.Subscribe
    import com.velocitypowered.api.event.connection.DisconnectEvent
    import com.velocitypowered.api.event.connection.PostLoginEvent
    import java.util.*
    
    class PlayerDataListener(private val database: Database) {
        @Subscribe
        suspend fun onPlayerJoinEvent(event: PostLoginEvent) {
            val player = event.player
            val playerData = database.getDataFromPlayer(player)
            playerData.name = player.username
            playerData.lastJoinDate = Date()
            database.saveData(player, playerData)
        }
    
        @Subscribe
        suspend fun onPlayerQuitEvent(event: DisconnectEvent) {
            val player = event.player
            val playerData = database.getDataFromPlayer(player)
            playerData.name = player.username
            playerData.lastQuitDate = Date()
            database.saveData(player, playerData)
        }
    }
    ````

=== "Minestom"

    ````kotlin
    import net.minestom.server.event.player.PlayerDisconnectEvent
    import net.minestom.server.event.player.PlayerLoginEvent
    import java.util.*
    
    class PlayerDataListener(private val database: Database) {
        suspend fun onPlayerJoinEvent(event: PlayerLoginEvent) {
            val player = event.player
            val playerData = database.getDataFromPlayer(player)
            playerData.name = player.username
            playerData.lastJoinDate = Date()
            database.saveData(player, playerData)
        }
    
        suspend fun onPlayerQuitEvent(event: PlayerDisconnectEvent) {
            val player = event.player
            val playerData = database.getDataFromPlayer(player)
            playerData.name = player.username
            playerData.lastQuitDate = Date()
            database.saveData(player, playerData)
        }
    }
    ````

=== "Fabric"

    ````kotlin
    import net.minecraft.entity.Entity
    import net.minecraft.entity.player.PlayerEntity
    import net.minecraft.util.Hand
    import net.minecraft.util.hit.EntityHitResult
    import net.minecraft.world.World
    import java.util.*
    
    class PlayerDataListener(private val database: Database) {
          suspend fun onPlayerAttackEvent(
            player: PlayerEntity,
            world: World,
            hand: Hand,
            entity: Entity,
            hitResult: EntityHitResult?
        ) {
           val playerData = database.getDataFromPlayer(player)
           playerData.name = player.name.toString()
           playerData.lastJoinDate = Date()
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
=== "Velocity"

     Instead of using ``register``, use the provided extension method ``registerSuspend`` to allow
    suspendable functions in your listener. Please notice, that timing measurements are no longer accurate for suspendable functions.

    ````kotlin
    import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
    import com.github.shynixn.mccoroutine.velocity.registerSuspend
    import com.google.inject.Inject
    import com.velocitypowered.api.event.Subscribe
    import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
    import com.velocitypowered.api.plugin.Plugin
    import com.velocitypowered.api.proxy.ProxyServer
    
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
        }
    }
    ````

=== "Minestom"

    Instead of using ``addListener``, use the provided extension method ``addSuspendingListener`` to allow
    suspendable functions in your listener. Please notice, that timing measurements are no longer accurate for suspendable functions.

    ```kotlin
    import com.github.shynixn.mccoroutine.minestom.addSuspendingListener
    import com.github.shynixn.mccoroutine.minestom.launch
    import com.github.shynixn.mccoroutine.minestom.sample.extension.impl.Database
    import com.github.shynixn.mccoroutine.minestom.sample.extension.impl.PlayerDataListener
    import net.minestom.server.MinecraftServer
    import net.minestom.server.event.player.PlayerLoginEvent
    
    fun main(args: Array<String>) {
        val minecraftServer = MinecraftServer.init() 
        minecraftServer.launch {
            val database = Database()
            // Minecraft Main Thread
            database.createDbIfNotExist()
    
            val listener = PlayerDataListener(database)
            MinecraftServer.getGlobalEventHandler()
                .addSuspendingListener(minecraftServer, PlayerLoginEvent::class.java) { e ->
                    listener.onPlayerJoinEvent(e)
                }
        }
    
        minecraftServer.start("0.0.0.0", 25565)
    }
    ```

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
            // Minecraft Main Thread
            val database = Database()
            database.createDbIfNotExist()

            val listener = PlayerDataListener(database)
            val mod = this
            AttackEntityCallback.EVENT.register(AttackEntityCallback { player, world, hand, entity, hitResult ->
                mod.launch {
                    listener.onPlayerAttackEvent(player, world, hand, entity, hitResult)
                }
                ActionResult.PASS
            })
        }
    }
    ````

### Test the Listener

Join and leave your server to observe ``getDataFromPlayer`` and ``saveData`` messages getting printed to your server log.
