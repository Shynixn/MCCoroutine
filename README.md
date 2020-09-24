# MCCoroutine

MCCoroutine is an extension to bukkit server implementations (Spigot, Paper, etc.) to use Kotlin Coroutines (also called async/await) pattern for
all common operations.

**A short listener example:**

```kotlin
// A new extension function 
server.pluginManager.registerSuspendingEvents(PlayerConnectListener(this, cache), this)
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
* Connection to events, commands, schedulers and packet stream
* Coroutine LifeCycle scope for plugins (supports plugin reloading)
* No NMS
* Support for Minecraft 1.7 - Latest
* Support for Java 8

## Installation

1. Include the dependency to StructureBlockLib

**Maven**
```xml
<dependency>
     <groupId>com.github.shynixn.mccoroutine</groupId>
     <artifactId>mccoroutine-bukkit-api</artifactId>
     <version>0.0.1</version>
     <scope>provided</scope>
</dependency>
```
**Gradle**

```xml
dependencies {
    compileOnly("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:0.0.1")
}
```

**Jar File**

[MCCoroutine.jar](http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.github.shynixn.structureblocklib&a=structureblocklib-bukkit-api&v=LATEST)

## Getting started 

**Introduction**

Asynchronous or non-blocking programming is the new reality. Whether we're creating server-side, desktop or mobile applications, it's important that we provide an experience that is not only fluid from the user's perspective, but scalable when needed.

There are many approaches to this problem, and in Kotlin we take a very flexible one by providing Coroutine support at the language level and delegating most of the functionality to libraries, much in line with Kotlin's philosophy.

Source:
(https://github.com/JetBrains/kotlin-web-site/blob/master/pages/docs/reference/coroutines-overview.md, Date: [09/11/2018], Licence copied to LICENCE).

**Notes**

This library was created for my plugins **PetBlocks** and **BlockBall** as I had to deal with sometimes short, sometimes long running async operations and packet manipulation. Caching 
database results is an essential component of both and a cache miss should not block the server in any way.

## Code Examples

* If you need more information, check out the sample implementation of a plugin using MCCoroutine in the `mccoroutine-bukkit-sample` folder.

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

Plugin plugin;
server.pluginManager.registerSuspendingEvents(PlayerConnectListener(), plugin)
```
