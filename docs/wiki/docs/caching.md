# Adding suspending caches and background tasks

This guide continues the guide 'Creating a new Plugin' and describes how the caching strategy ``lazy loading`` can be
used together with coroutines.

### 1. Add a simple cache

When taking a look at the ``Database`` implementation from before, we can observe quite a lot of redundant database
accesses when a player rejoins a server in a very short timeframe.

For this, we put a ``lazy-loading`` cache in front of the  ``Database`` implementation.

````kotlin
import kotlinx.coroutines.Deferred
import org.bukkit.entity.Player

class DatabaseCache(private val database: Database) {
    private val cache = HashMap<Player, Deferred<PlayerData>>()

    suspend fun getDataFromPlayer(player: Player): PlayerData {
    }
}
````

### 2. Deferred PlayerData

Instead of using the type ``PlayerData`` directly, we use the type ``Deferred`` which is the representation of a
non-blocking job which has got ``PlayerData`` as result. This means we essentially store the job of retrieving data from
the database into the cache.

````kotlin
import kotlinx.coroutines.*
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class DatabaseCache(private val database: Database, private val plugin: Plugin) {
    private val cache = HashMap<Player, Deferred<PlayerData>>()

    suspend fun getDataFromPlayer(player: Player): PlayerData {
        return coroutineScope {
            if (!cache.containsKey(player)) {
                // Cache miss, create a new job
                cache[player] = async(Dispatchers.IO) {
                    database.getDataFromPlayer(player)
                }
            }

            // Await suspends the current context until the value of the ``Deferred`` job is ready.
            cache[player]!!.await()
        }
    }
}
````

### 3. Clear the cache

Clearing the cache is as simple as adding a ``clear`` method.

````kotlin
import kotlinx.coroutines.*
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class DatabaseCache(private val database: Database, private val plugin: Plugin) {
    private val cache = HashMap<Player, Deferred<PlayerData>>()

    fun clear() {
        cache.clear()
    }

    suspend fun getDataFromPlayer(player: Player): PlayerData {
        return coroutineScope {
            if (!cache.containsKey(player)) {
                // Cache miss, create a new job
                cache[player] = async(Dispatchers.IO) {
                    database.getDataFromPlayer(player)
                }
            }

            // Await suspends the current context until the value of the ``Deferred`` job is ready.
            cache[player]!!.await()
        }
    }
}
````

### 4. Adding auto save of cache

It is possible to add a new suspendable repeatable background task to save the cached data every 10 minutes. 

````kotlin
import com.github.shynixn.mccoroutine.launch
import kotlinx.coroutines.*
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class DatabaseCache(private val database: Database, private val plugin: Plugin) {
    private val cache = HashMap<Player, Deferred<PlayerData>>()

    init {
        // This plugin.launch launches a new scope in the minecraft server context which can be understood
        // to be a background task and behaves in a similar way to Bukkit.getScheduler().runTask(plugin, Runnable {  })
        plugin.launch {
            // This background task is a repeatable task which in this case is an endless loop. The endless loop
            // is automatically stopped by MCCoroutine once you reload your plugin.
            while (true) {
                // Save all cached player data every 10 minutes.
                for (player in cache.keys.toTypedArray()) {
                    database.saveData(player, cache[player]!!.await())

                    // Remove player when no longer online
                    if (!player.isOnline) {
                        cache.remove(player)
                    }
                }

                // Suspending the current context is important in this case otherwise the minecraft thread will only execute this
                // endless loop as it does not have time to execute other things. Delay gives the thread time to execute other things.
                delay(10 * 60 * 1000) // 10 minutes
            }
        }
    }

    fun clear() {
        cache.clear()
    }

    suspend fun getDataFromPlayer(player: Player): PlayerData {
        return coroutineScope {
            if (!cache.containsKey(player)) {
                // Cache miss, create a new job
                cache[player] = async(Dispatchers.IO) {
                    database.getDataFromPlayer(player)
                }
            }

            // Await suspends the current context until the value of the ``Deferred`` job is ready.
            cache[player]!!.await()
        }
    }
}
````

### 5. Update PlayerDataListener

It is no longer necessary to manually call save as auto save is put in place. 
Also, the cache automatically clears itself up every 10 minutes. 

````kotlin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*

class PlayerDataListener(private val database : DatabaseCache) : Listener {
    @EventHandler
    suspend fun onPlayerJoinEvent(playerJoinEvent: PlayerJoinEvent) {
        val player = playerJoinEvent.player
        val playerData = database.getDataFromPlayer(player)
        playerData.name = player.name
        playerData.lastJoinDate = Date()
    }
}
````

### 4. Test the Cache

Join and leave your server to observe changes on your database.
