# Adding Suspending Listeners

This guide continues the guide 'Creating a new Plugin' and describes how listeners can be used to load and save player data.

### 1. Create the Listener class

Create a traditional listener and add suspend to all functions where you perform suspendable operations (e.g. calling other
suspend functions). You can mix suspendable and non suspendable functions in listeners.

````kotlin

class PlayerDataListener(private val database : Database) : Listener {
    @EventHandler
    suspend fun onPlayerJoinEvent(playerJoinEvent: PlayerJoinEvent) {
       val playerData = database.getDataFromPlayer(playerJoinEvent.player)
       playerData.name = player.name 
       playerData.lastJoinDate = Date()
       database.saveData(player, playerData)
    }
    
    @EventHandler
    suspend fun onPlayerQuitEvent(playerQuitEvent: PlayerQuitEvent) {
        val playerData = database.getDataFromPlayer(playerQuitEvent.player)
        playerData.name = player.name
        playerData.lastQuitDate = Date()
        database.saveData(player, playerData)
    }
}
````

### 2. Connect JavaPlugin and PlayerDataListener

Instead of using ``registerEvents``, use the provided extension method ``registerSuspendingEvents`` to allow
suspendable functions in your listener. Please consider, that timing measurements are no longer accurate for suspendable functions.

````kotlin
class MCCoroutineSamplePlugin : SuspendingJavaPlugin() {
    private val database = Database()

    override suspend fun onEnableAsync() {
        database.createDbIfNotExist()
        server.pluginManager.registerSuspendingEvents(PlayerDataListener(database), plugin)
    }

    override suspend fun onDisableAsync() {
    }
}
````

### 3. Test the Listener

Join and leave your server to observe ``getDataFromPlayer`` and ``saveData`` messages print to your server log.
Extend it with real database operations to get familiar with how it works.

The next page continuous by adding command executors to the plugin.
