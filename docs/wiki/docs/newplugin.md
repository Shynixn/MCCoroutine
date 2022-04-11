# Suspending Plugin

This guide explains how Kotlin Coroutines can be used in minecraft plugins in various ways using MCCoroutine. 
For this, a new plugin is developed from scratch to handle asynchronous and synchronous code.

!!! note "Important"
    Make sure you have already installed MCCoroutine. See [Installation](/gettingstarted) for details.

## Plugin Main class

MCCoroutine does not need to be called explicitly in your plugin main class. It is started implicitly when you use it for the first time and
disposed automatically when you reload your plugin. 

=== "Bukkit"

    The first decision for Bukkit API based plugins is to decide between ``JavaPlugin`` or ``SuspendingJavaPlugin``, which is a new base
    class extending ``JavaPlugin``.

    If you want to perform async operations or call other suspending functions from your plugin class, go with the newly
    available type ``SuspendingJavaPlugin`` otherwise use ``JavaPlugin``.

    ````kotlin
    import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
    
    class MCCoroutineSamplePlugin : SuspendingJavaPlugin() {
        override suspend fun onEnableAsync() {
            // Minecraft Main Thread
        }
    
        override suspend fun onDisableAsync() {
            // Minecraft Main Thread
        }
    }
    ````

    !!! note "How onEnableAsync works"
        The implementation which calls the ``onEnableAsync`` function manipulates the Bukkit Server implementation in the
        following way:
        If a context switch is made, it blocks the entire minecraft main thread until the context is given back. This means,
        in this method, you can switch contexts as you like but the plugin is not considered enabled until the context is given
        back.
        It allows for a clean startup as the plugin is not considered "enabled" until the context is given back.
        Other plugins which are already enabled, may or may not already perform work in the background.
        Plugins, which may get enabled in the future, wait until this plugin is enabled.


=== "BungeeCord"

    The first decision for BungeeCord API based plugins is to decide between ``Plugin`` or ``SuspendingPlugin``, which is a new base
    class extending ``Plugin``.

    If you want to perform async operations or call other suspending functions from your plugin class, go with the newly
    available type ``SuspendingPlugin`` otherwise use ``Plugin``.

    ````kotlin
    import com.github.shynixn.mccoroutine.bungeecord.SuspendingPlugin

    class MCCoroutineSamplePlugin : SuspendingPlugin() {
        override suspend fun onEnableAsync() {
            // BungeeCord Startup Thread
        }
    
        override suspend fun onDisableAsync() {
            // BungeeCord Shutdown Thread (Not the same as the startup thread)
        }
    }
    ````

    !!! note "How onEnableAsync works"
        The implementation which calls the ``onEnableAsync`` function manipulates the BungeeCord Server implementation in the
        following way:
        If a context switch is made, it blocks the entire bungeecord startup thread until the context is given back. This means,
        in this method, you can switch contexts as you like but the plugin is not considered enabled until the context is given
        back.
        It allows for a clean startup as the plugin is not considered "enabled" until the context is given back.
        Other plugins which are already enabled, may or may not already perform work in the background.
        Plugins, which may get enabled in the future, wait until this plugin is enabled.

=== "Sponge"

    The first decision for Sponge API based plugins is to decide, if you want to call other suspending functions from your plugin class.
    If so, add a field which injects the type ``SuspendingPluginContainer``. This turns your main class into a suspendable listener.

    ````kotlin
    import com.github.shynixn.mccoroutine.sponge.SuspendingPluginContainer
    @Plugin(
        id = "mccoroutinesample",
        name = "MCCoroutineSample",
        description = "MCCoroutineSample is sample plugin to use MCCoroutine in Sponge."
    )
    class MCCoroutineSamplePlugin {
        @Inject
        private lateinit var suspendingPluginContainer: SuspendingPluginContainer
    
        @Listener
        suspend fun onEnable(event: GameStartedServerEvent) {
            // Minecraft Main Thread
        }
    
        @Listener
        suspend fun onDisable(event: GameStoppingServerEvent) {
            // Minecraft Main Thread
        }
    }
    ````

## Calling a Database from Plugin Main class

Create a class containing properties of data, which we want to store into a database.

````kotlin
class PlayerData(var uuid: UUID, var name: String, var lastJoinDate: Date, var lastQuitDate : Date) {
}
````

Create a class ``Database``, which is responsible to store/retrieve this data into/from a database. 
Here, it is important that we perform all IO calls on async threads and returns on the minecraft main thread.

````kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player
import java.util.*

class Database() {
    suspend fun createDbIfNotExist() {
        println("[createDbIfNotExist] Start on minecraft thread " + Thread.currentThread().id)
        withContext(Dispatchers.IO){
            println("[createDbIfNotExist] Creating database on database io thread " + Thread.currentThread().id)
            // ... create tables
        }
        println("[createDbIfNotExist] End on minecraft thread " + Thread.currentThread().id)
    }

    suspend fun getDataFromPlayer(player : Player) : PlayerData {
        println("[getDataFromPlayer] Start on minecraft thread " + Thread.currentThread().id)
        val playerData = withContext(Dispatchers.IO) {
            println("[getDataFromPlayer] Retrieving player data on database io thread " + Thread.currentThread().id)
            // ... get from database by player uuid or create new playerData instance.
            PlayerData(player.uniqueId, player.name, Date(), Date())
        }

        println("[getDataFromPlayer] End on minecraft thread " + Thread.currentThread().id)
        return playerData;
    }
  
    suspend fun saveData(player : Player, playerData : PlayerData) {
        println("[saveData] Start on minecraft thread " + Thread.currentThread().id)

        withContext(Dispatchers.IO){
            println("[saveData] Saving player data on database io thread " + Thread.currentThread().id)
            // insert or update playerData
        }

        println("[saveData] End on minecraft thread " + Thread.currentThread().id)
    }
}
````

Create a new instance of the database and call it in your main class.

=== "Bukkit"

    ````kotlin
    import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
    
    class MCCoroutineSamplePlugin : SuspendingJavaPlugin() {
        private val database = Database()
    
        override suspend fun onEnableAsync() {
            // Minecraft Main Thread
            database.createDbIfNotExist()
        }
    
        override suspend fun onDisableAsync() {
        }
    }
    ````

=== "BungeeCord"

    ````kotlin
    import com.github.shynixn.mccoroutine.bungeecord.SuspendingPlugin

    class MCCoroutineSamplePlugin : SuspendingPlugin() {
        private val database = Database()

        override suspend fun onEnableAsync() {
            // BungeeCord Startup Thread
            database.createDbIfNotExist()
        }
    
        override suspend fun onDisableAsync() {
            // BungeeCord Shutdown Thread (Not the same as the startup thread)
        }
    }
    ````

=== "Sponge"

    ````kotlin
    import com.github.shynixn.mccoroutine.sponge.SuspendingPluginContainer
    @Plugin(
        id = "mccoroutinesample",
        name = "MCCoroutineSample",
        description = "MCCoroutineSample is sample plugin to use MCCoroutine in Sponge."
    )
    class MCCoroutineSamplePlugin {
        private val database = Database()
        @Inject
        private lateinit var suspendingPluginContainer: SuspendingPluginContainer
    
        @Listener
        suspend fun onEnable(event: GameStartedServerEvent) {
            // Minecraft Main Thread
            database.createDbIfNotExist()
        }
    
        @Listener
        suspend fun onDisable(event: GameStoppingServerEvent) {
            // Minecraft Main Thread
        }
    }
    ````


## Test the Plugin

Start your server to observe the ``createDbIfNotExist`` messages getting printed to your server log.
Extend it with real database operations to get familiar with how it works.
