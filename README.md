# MCCoroutine

MCCoroutine is an extension to bukkit server implementations (Spigot, Paper, etc.) to use Kotlin Coroutines (also called async/await) pattern for
all common operations.

JavaDocs: https://shynixn.github.io/MCCoroutine/apidocs/

You can find the original article of the repository [here](https://github.com/Shynixn/MCCoroutine/blob/master/ARTICLE.md).

**A short listener example:**

```kotlin
// A new extension function 
server.pluginManager.registerSuspendingEvents(PlayerConnectListener(), plugin)
```

```kotlin
@EventHandler
suspend fun onPlayerJoinEvent(playerJoinEvent: PlayerJoinEvent) {
    val player = player
    // Long running operation to database is automatically suspended and continued.
    val userData = database.getUserDataFromPlayer(player)
    // Userdata was loaded asynchronous from the database and is now ready.
    println(userData.killCount)
}
```
   
## Features

* Full implementation of Kotlin Coroutines (async/await)
* Extension functions for already established functions
* Connection to events, commands, schedulers
* Coroutine LifeCycle scope for plugins (supports plugin reloading)
* No NMS
* Support for Minecraft 1.7 - Latest
* Support for Java 8

## Installation

Include the dependency to MCCoroutine

**Maven**
```xml
<dependency>
     <groupId>com.github.shynixn.mccoroutine</groupId>
     <artifactId>mccoroutine-bukkit-api</artifactId>
     <version>0.0.4</version>
     <scope>provided</scope>
</dependency>
```
**Gradle**

```xml
dependencies {
    compileOnly("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:0.0.4")
}
```

**Jar File**

[MCCoroutine.jar](http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.github.shynixn.mccoroutine&a=mccoroutine-bukkit-api&v=LATEST)

## Getting started 

**Introduction**

Asynchronous or non-blocking programming is the new reality. Whether we're creating server-side, desktop or mobile applications, it's important that we provide an experience that is not only fluid from the user's perspective, but scalable when needed.

There are many approaches to this problem, and in Kotlin we take a very flexible one by providing Coroutine support at the language level and delegating most of the functionality to libraries, much in line with Kotlin's philosophy.

Source:
(https://github.com/JetBrains/kotlin-web-site/blob/master/pages/docs/reference/coroutines-overview.md, Date: [09/11/2018], Licence copied to LICENCE).

**Notes**

This library was created for my plugins **PetBlocks** and **BlockBall** as I had to deal with sometimes short, sometimes long running async operations. Caching 
database results is an essential component of both and a cache miss should not block the server in any way.

## Code Examples

* If you need more information, check out the sample implementation of a plugin using MCCoroutine in the `mccoroutine-bukkit-sample` folder.
* In detail coroutines work with context switches but for simplification cases this guide uses the term `async operations` instead of `context switches`.

### Event Listener, Commands, Schedulers

##### Registering a suspending event listener

* Create an ordinary listener and simple prepend ``suspend`` to all functions where you are going to perform async operations. If you do not perform async operations, you do not need to append it.

```kotlin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerConnectListener : Listener {
    @EventHandler
    suspend fun onPlayerJoinEvent(playerJoinEvent: PlayerJoinEvent) {
    }
}
```

* Register the listener using the extension method for the ``PluginManager``.

```kotlin
import com.github.shynixn.mccoroutine.registerSuspendingEvents

Plugin plugin
server.pluginManager.registerSuspendingEvents(PlayerConnectListener(), plugin)
```

##### Registering a suspending command executor

* If you need to perform async operations, implement the SuspendingCommandExecutor instead of the standard command executor.

```kotlin
import com.github.shynixn.mccoroutine.SuspendingCommandExecutor

class AdminCommandExecutor: SuspendingCommandExecutor {
    override suspend fun onCommand(sender: CommandSender,command: Command,label: String,args: Array<out String>): Boolean {
        return false
    }
}
```

* Register the command executor using the extension method for the ``PluginCommand``.

```kotlin
import com.github.shynixn.mccoroutine.setSuspendingExecutor

Plugin plugin
String commandName
plugin.getCommand(commandName)!!.setSuspendingExecutor(AdminCommandExecutor())
```

##### Schedulers

* Launching a sync (Bukkit Thread) scheduler.

```kotlin
import com.github.shynixn.mccoroutine.launch

Plugin plugin
plugin.launch {
    // Delayed task.
    // Delay frees the main thread for the amount of milliseconds and does not block.
    delay(500)

    // Repeating task.
    while (true) {
        delay(20)
    }

    // Task is over.
}
```

* Launching an async scheduler.

```kotlin
import com.github.shynixn.mccoroutine.launchAsync

Plugin plugin
plugin.launchAsync {
    // Delayed task.
    // Delay frees the main thread for the amount of milliseconds and does not block.
    delay(500)

    // Repeating task.
    while (true) {
        delay(20)
    }

    // Task is over.
}
```

#### Other scope operations

```kotlin
import com.github.shynixn.mccoroutine.scope

Plugin plugin
val scope = plugin.scope
```

### Recommend extension methods (These will be used later in this guide)

As you may have noticed, almost every call to the api needs a plugin instance which kind of
hurts using the api. In order to make it easier, add the following extension methods to your plugin.

```kotlin
// Just put these functions global anywhere in your plugin.
import com.github.shynixn.mccoroutine.launchAsync
import com.github.shynixn.mccoroutine.launch
import com.github.shynixn.mccoroutine.asyncDispatcher
import com.github.shynixn.mccoroutine.minecraftDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

fun launch(f: suspend CoroutineScope.() -> Unit) {
    JavaPlugin.getPlugin(YourPluginClass::class.java).launch(f)
}

fun launchAsync(f: suspend CoroutineScope.() -> Unit) {
    JavaPlugin.getPlugin(YourPluginClass::class.java).launchAsync(f)
}

val Dispatchers.minecraft: CoroutineContext
    get() {
        return JavaPlugin.getPlugin(YourPluginClass::class.java).minecraftDispatcher
    }

val Dispatchers.async: CoroutineContext
    get() {
        return JavaPlugin.getPlugin(YourPluginClass::class.java).asyncDispatcher
    }
```

```kotlin
// Now we can use the dispatchers and launch functions everywhere.
fun someFunctionInYourProject(){
    launch {
        // Delayed task.
        // Delay frees the main thread for the amount of milliseconds and does not block.
        delay(500)
    }
}
```

### Async operations (and context switches)

##### Loading data async from a database on player join

* Let's assume we have got a PlayerJoinEvent where we want to load some data async from a database. (We ignore the fact
that a event called AsyncPlayerPreLoginEvent already exists.)

1. Add the recommend extension methods above
2. Register and write the event class as mentioned above.

```kotlin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
class PlayerConnectListener(
// Some DataBase class which does jdbc operations.
private val database : Database) : Listener {
    @EventHandler
    suspend fun onPlayerJoinEvent(playerJoinEvent: PlayerJoinEvent) {
        
        // withContext is a method to switch to a given coroutine context.
        // In this case it switches the context to the async bukkit scheduler.
        // After switching the calling thread is suspended and does other pending work. 
        // This call does not block.
        val userData = withContext(Dispatchers.async) {
            // We can confirm that this code is executed on a different thread.
            println(Bukkit.isPrimaryThread().toString())
            database.getUserDataFromPlayer(playerJoinEvent.player)
        }
        
        // The Bukkit Thread will automatically continue here after userData has been loaded.
        // It is possible that other events happened in the mean time but now the Bukkit Thread has got time for us.
        // Confirm we are on the primary thread.
        println(Bukkit.isPrimaryThread().toString())
    
        // Remove the following line because it will not compile anyway.
        // This is just for you to notice that cancelling events or changing the result is no longer possible 
        // after a context switch. Makes sense because you cannot change what has already happened.
        playerJoinEvent.isCancelled = true

        // Do something with the user data ..
    }
}
```

##### Loading data from multiple different sources parallel

```kotlin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import kotlinx.coroutines.coroutineScope

class PlayerConnectListener(
// Some DataBase class which does jdbc operations.
private val database1 : Database, private val database2 : Database) : Listener {
    @EventHandler
    suspend fun onPlayerJoinEvent(playerJoinEvent: PlayerJoinEvent) {
        // In order to use async we need to be in a new coroutine scope.
        val result = coroutineScope {
            val data1Job = async(Dispatchers.async) {
                database1.getUserDataFromPlayer(playerJoinEvent.player)
            }

            val data2Job = async(Dispatchers.async) {
                database2.getOtherData(playerJoinEvent.player)
            }
            
            // Both jobs are now running in parallel. 
            // Await all waits for both tasks to be finished.
            awaitAll(data1, data2)
            // Await does now return the data immidately.
            Pair(data1.await(), data2Job.await())
        }
    }
}
```

##### How to handle caching 

```kotlin
// Assume we have got a database implementation.
class FakeDatabase {
    /**
     *  Simulates a getUserData call to a real database by delaying the result.
     */
    fun getUserDataFromPlayer(player: Player): UserData {
        Thread.sleep(5000)
        val userData = UserData()
        userData.amountOfEntityKills = 20
        userData.amountOfPlayerKills = 30
        return userData
    }
}

class UserDataCache(private val plugin: Plugin, private val fakeDatabase: FakeDatabase) {
    private val cache = HashMap<UUID, Deferred<UserData>>()

    /**
     * Clears the player cache.
     */
    fun clearCache(player: Player) {
        cache.remove(player.uniqueId)
    }

    /**
     * Gets the user data from the player.
     */
    suspend fun getUserDataFromPlayer(player: Player): UserData {
        // Open a coroutine scope because we want to manage waiting with async
        return coroutineScope {
            // Still on bukkit primary thread.
            // If the runtime cache does not have the userdata -> Cache miss.
            if (!cache.containsKey(player.uniqueId)) {
                // Still on bukkit primary thread.
                // Create a new job to get the user data from the database and cache it.
                // Thread is now suspended and does other work. 
                // It is now possible that since the primary thread is free, it can call getUserDataFromPlayer again
                // before the operation below has finished. However, this time it does not enter this branch as the job is already cached.
                cache[player.uniqueId] = async(Dispatchers.async) {
                    // Async thread.
                    fakeDatabase.getUserDataFromPlayer(player)
                }
            }
          
            // Suspends the calling thread until the data has been loaded.
            // If the data has already been loaded, it returns immediately the result without suspension. 
            // It is possible that the bukkit primary thread is suspended multiple times here and continuous each time once the value
            // is there.
            cache[player.uniqueId]!!.await()
        }
    }
}
```

##### How to connect any other sync Api

```kotlin
// Assume we want to provide our userdata stats via placeholder api.
class PlaceHolderApiConnector(private val cache : UserDataCache) {
    override fun onPlaceholderRequest(player: Player?, text: String?): String? {
        var result: String? = null
        
        // If the user data is already fetched and cached, the result will not be
        // null because zero context switches are going to happen.
        // If the user data is not fetched. Simply return null and start fetching the data.
        // The unconfined dispatcher does not perform any context switch and stays on the same calling thread.
        plugin.launch(Dispatchers.Unconfined){
            result = onPlaceHolderRequestSuspend(player, text)
        }

        return result
    }
    
    private suspend fun onPlaceHolderRequestSuspend(player: Player?, text: String?): String? {
        if(player == null){
            return null
        }

        val userData =  cache.getUserDataFromPlayer(player!!)

        // ..
    }
}
```

## Shipping and Running

* In order to use the MCCoroutine Api on your server, you need to put the implementation of the Api on your server.
* This can only be achieved by shipping the following dependencies with your plugin.

**Maven**
```xml
<dependency>
     <groupId>com.github.shynixn.mccoroutine</groupId>
     <artifactId>mccoroutine-bukkit-core</artifactId>
     <version>0.0.4</version>
     <scope>compile</scope>
</dependency>
<dependency>
     <groupId>org.jetbrains.kotlinx</groupId>
     <artifactId>kotlinx-coroutines-core</artifactId>
     <version>1.x.x</version> 
     <scope>compile</scope>
</dependency>
<dependency>
     <groupId>org.jetbrains.kotlin</groupId>
     <artifactId>kotlin-reflect</artifactId>
     <version>1.x.x</version> 
     <scope>compile</scope>
</dependency>
```
**Gradle**

```xml
dependencies {
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:0.0.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.x.x")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.x.x")
}
```

## Contributing

* Fork the MCCoroutine project on github and clone it to your local environment
* Install Java 8+
* Execute gradle sync for dependencies

## Licence

The source code is licensed under the MIT license. 
