# Suspending Plugin

This guide explains how Kotlin Coroutines can be used in minecraft plugins in various ways using MCCoroutine. 
For this, a new plugin is developed from scratch to handle asynchronous and synchronous code.

!!! note "Important"
    Make sure you have already installed MCCoroutine. See [Installation](/gettingstarted) for details.

## Plugin Main class

MCCoroutine does not need to be called explicitly in your plugin main class. It is started implicitly when you use it for the first time and
disposed automatically when you reload your plugin. 


Here the first decision is to decide between extending ``JavaPlugin`` or ``SuspendingJavaPlugin`` which is a new base
class extending ``JavaPlugin``.

Here is an easy rule to follow:

* If you want to perform async operations or call other suspending functions from your plugin class, go with the newly
  available type ``SuspendingJavaPlugin`` otherwise use ``JavaPlugin``.

````kotlin
import com.github.shynixn.mccoroutine.SuspendingJavaPlugin

class MCCoroutineSamplePlugin : SuspendingJavaPlugin() {
    override suspend fun onEnableAsync() {
    }

    override suspend fun onDisableAsync() {
    }
}
````

!!! note "How onEnableAsync works"
    The implementation which calls the ``onEnableAsync`` function manipulates the Bukkit Server implementation in the
    following way:
    If a context switch is made, it blocks the entire minecraft-server thread until the context is given back. This means
    in this method you can switch contexts as you like but the plugin is not considered enabled until the context is given
    back.
    It allows for a clean startup as the plugin is not considered "enabled" until the context is given back.
    Other plugins which are already enabled, may or may not already perform work in the background.
    Plugins which may get enabled in the future, wait until this plugin is enabled.

## 3. Create Database and PlayerData class

````kotlin
class PlayerData(var uuid: UUID, var name: String, var lastJoinDate: Date, var lastQuitDate : Date) {
}
````

Here, we perform all database operations on the IO context provided by Kotlin Coroutines.
The result is automatically returned to the Bukkit primary thread. 

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

### 4. Connect JavaPlugin and Database

Create a new instance of the database and call it in the onEnable function.

````kotlin
import com.github.shynixn.mccoroutine.SuspendingJavaPlugin

class MCCoroutineSamplePlugin : SuspendingJavaPlugin() {
    private val database = Database()
  
    override suspend fun onEnableAsync() {
        database.createDbIfNotExist()
    }

    override suspend fun onDisableAsync() {
    }
}
````

### 5. Test the Java Plugin

Start your server to observe the ``createDbIfNotExist`` messages print to your server log.
Extend it with real database operations to get familiar with how it works.

The next page continuous by adding listeners to the plugin.
