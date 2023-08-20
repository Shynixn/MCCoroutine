# Suspending Caches, Background Repeating Tasks

This page explains how you can create a ``lazy-loading`` cache using Kotlin Coroutines.

In minecraft plugins, players can perform many actions in a short time period. If plugins want to keep track of them and store
every action in the database, creating a new database call for every single action may cause performance problems. Therefore, caches are often
implemented, which is a lot easier when using coroutines.

!!! note "Important"
    The following code examples are for Bukkit, but work in a similar way in other mccoroutine implementations.

## Implementing a Cache

When taking a look at the ``Database`` implementation below, we can observe quite a lot of redundant database
accesses when a player rejoins a server in a very short timeframe.

For this, we put a ``lazy-loading`` cache in front of the  ``Database`` implementation.

````kotlin
class Database() {
    fun createDbIfNotExist() {
        // ... SQL calls
    }

    fun getDataFromPlayer(player : Player) : PlayerData {
        // ... SQL calls
    }

    fun saveData(player : Player, playerData : PlayerData) {
        // ... SQL calls
    }
}
````

````kotlin
import kotlinx.coroutines.Deferred
import org.bukkit.entity.Player

class DatabaseCache(private val database: Database) {
    private val cache = HashMap<Player, Deferred<PlayerData>>()

    suspend fun getDataFromPlayer(player: Player): PlayerData {
    }
}
````

### Deferred PlayerData

Instead of using the type ``PlayerData`` directly, we use the type ``Deferred``, which is the representation of a
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

            // Await suspends the current context until the value of the Deferred job is ready.
            cache[player]!!.await()
        }
    }
}
````

### Implementing cache clearing

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

## Background Repeating Tasks

After introducing a cache, we can implement a new suspendable background task to save the cached data every 10 minutes.

````kotlin
import com.github.shynixn.mccoroutine.bukkit.launch
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
