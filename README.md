# MCCoroutine [![Build Status](https://maven-badges.herokuapp.com/maven-central/com.github.shynixn.mccoroutine/mccoroutine-bukkit-api/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.github.shynixn.mccoroutine/mccoroutine-bukkit-api) [![GitHub license](http://img.shields.io/badge/license-MIT-blue.svg?style=flat-square)](https://raw.githubusercontent.com/Shynixn/MCCoroutine/master/LICENSE)

| branch        | status        |  version | download |
| ------------- | ------------- |  --------| ---------| 
| master        | [![Build Status](https://github.com/Shynixn/MCCoroutine/workflows/CI/badge.svg?branch=master)](https://github.com/Shynixn/MCCoroutine/actions)| ![GitHub license](https://img.shields.io/nexus/r/https/oss.sonatype.org/com.github.shynixn.mccoroutine/mccoroutine-bukkit-api.svg?style=flat-square)  |[Download latest release](https://repo1.maven.org/maven2/com/github/shynixn/mccoroutine/)|
| development   | [![Build Status](https://github.com/Shynixn/MCCoroutine/workflows/CI/badge.svg?branch=development)](https://github.com/Shynixn/MCCoroutine/actions) |![GitHub license](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.github.shynixn.mccoroutine/mccoroutine-bukkit-api.svg?style=flat-square) |  [Download snapshots](https://oss.sonatype.org/content/repositories/snapshots/com/github/shynixn/mccoroutine/) |

MCCoroutine is a library, which adds extensive support for Kotlin Coroutines for Minecraft Server environments.

Plugins for game servers and proxy servers often need to perform asynchronous operations (e.g. accessing databases) to
be scalable for a large amount of players. MCCoroutine brings the full power of Kotlin Coroutines to them by extending
the existing APIs with suspendable commands, events and schedules.

**Supported Game Servers:**

* Spigot
* Paper
* CraftBukkit
* SpongeVanilla
* SpongeForge

**Supported Proxies:**

* BungeeCord
* Waterfall
* Velocity

**Examples:**

```kotlin
// Allows to prepend suspend to any listener function.
class PlayerConnectListener : Listener {
    @EventHandler
    suspend fun onPlayerJoinEvent(playerJoinEvent: PlayerJoinEvent) {
    }
}
```

```kotlin
// Adds a new interface for suspendable command executors.
class AdminCommandExecutor: SuspendingCommandExecutor {
    override suspend fun onCommand(sender: CommandSender,command: Command,label: String,args: Array<out String>): Boolean {
        return false
    }
}
```

```kotlin
// Adds a new extension function to switch into a suspendable plugin coroutine.
fun bar() {
    plugin.launch {
        delay(1000)
        bob()
    }
}

private suspend fun bob() {
}
```

## Getting started

* [User Guide](https://shynixn.github.io/MCCoroutine/wiki/site/)
* [MCCoroutine JavaDocs for the Bukkit-API](https://shynixn.github.io/MCCoroutine/apidocs/mccoroutine-root/com.github.shynixn.mccoroutine.bukkit/index.html)
* [MCCoroutine JavaDocs for the Sponge-API](https://shynixn.github.io/MCCoroutine/apidocs/mccoroutine-root/com.github.shynixn.mccoroutine.sponge/index.html)
* [MCCoroutine JavaDocs for the BungeeCord-API](https://shynixn.github.io/MCCoroutine/apidocs/mccoroutine-root/com.github.shynixn.mccoroutine.bungeecord/index.html)
* [MCCoroutine JavaDocs for the Velocity-API](https://shynixn.github.io/MCCoroutine/apidocs/mccoroutine-root/com.github.shynixn.mccoroutine.velocity/index.html)
* [Article on custom frameworks](https://github.com/Shynixn/MCCoroutine/blob/master/ARTICLE.md)
   
## Donation 

Support development with a small tip :heart: :coffee:.

* Dogecoin Address: ``DAzt6RGAapkhbKD9uFKJgSR5vpfT9nSvKi``   
   
## Features

* Full implementation of Kotlin Coroutines (async/await)
* Extension functions for already established functions
* Connection to events, commands, schedulers
* Coroutine LifeCycle scope for plugins (supports plugin reloading)
* No NMS
* Support for Minecraft 1.7 - Latest
* Support for Java 8

## Contributing

* Fork the MCCoroutine project on github and clone it to your local environment
* Install Java 8+
* Execute gradle sync for dependencies

## Licence

The source code is licensed under the MIT license. 
