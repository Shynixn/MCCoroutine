# MCCoroutine 
[![Build Status](https://maven-badges.herokuapp.com/maven-central/com.github.shynixn.mccoroutine/mccoroutine-bukkit-api/badge.svg?style=flat-square
)](https://maven-badges.herokuapp.com/maven-central/com.github.shynixn.mccoroutine/mccoroutine-bukkit-api) [![GitHub license](http://img.shields.io/badge/license-MIT-blue.svg?style=flat-square)](https://raw.githubusercontent.com/Shynixn/MCCoroutine/master/LICENSE)

MCCoroutine is an extension to bukkit and sponge server implementations (Spigot, Paper, SpongeVanilla, SpongeForge etc.) to use Kotlin Coroutines (also called async/await) pattern for
all common operations.

JavaDocs: https://shynixn.github.io/MCCoroutine/apidocs/

You can find the original article of the repository [here](https://github.com/Shynixn/MCCoroutine/blob/master/ARTICLE.md).

If you are looking for examples using the Bukkit Api, you can find them [here](https://github.com/Shynixn/MCCoroutine/blob/master/README.md).

**A short listener example:**

* Please note that EventFilters are not available in listeners registered in this way.

```kotlin
// A new extension function 
Sponge.getEventManager().registerSuspendingListeners(plugin, PlayerConnectListener())
```

```kotlin
@Listener
suspend fun onPlayerJoinEvent(playerJoinEvent: ClientConnectionEvent.Join) {
    val player = player.targetEntity
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
* Coroutine LifeCycle scope for plugins
* No NMS
* Support for Minecraft 1.12
* Support for Java 8

## Installation

Include the dependency to MCCoroutine

**Maven**
```xml
<dependency>
     <groupId>com.github.shynixn.mccoroutine</groupId>
     <artifactId>mccoroutine-sponge-api</artifactId>
     <version>0.0.5</version>
     <scope>provided</scope>
</dependency>
```
**Gradle**

```xml
dependencies {
    compileOnly("com.github.shynixn.mccoroutine:mccoroutine-sponge-api:0.0.5")
}
```

**Jar File**

[MCCoroutine.jar](http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.github.shynixn.mccoroutine&a=mccoroutine-sponge-api&v=LATEST)

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

* If you need more information, check out the sample implementation of a plugin using MCCoroutine in the `mccoroutine-sponge-sample` folder.
* In detail coroutines work with context switches but for simplification cases this guide uses the term `async operations` instead of `context switches`.

### Event Listener, Commands, Schedulers

##### Registering a suspending event listener

* Create an ordinary listener and simple prepend ``suspend`` to all functions where you are going to perform async operations. If you do not perform async operations, you do not need to append it.

```kotlin
class PlayerConnectListener : Listener {
    @Listener
    suspend fun onPlayerJoinEvent(playerJoinEvent: ClientConnectionEvent.Join) {
    }
}
```

* Register the listener using the extension method for the ``EventManager``.

```kotlin
import com.github.shynixn.mccoroutine.registerSuspendingListeners

Plugin plugin
Sponge.getEventManager().registerSuspendingListeners(plugin, PlayerConnectListener())
```

##### Registering a suspending command executor

* If you need to perform async operations, implement the SuspendingCommandExecutor instead of the standard command executor.

```kotlin
import com.github.shynixn.mccoroutine.SuspendingCommandExecutor

class AdminCommandExecutor: SuspendingCommandExecutor {
    override suspend fun execute(src: CommandSource, args: CommandContext): CommandResult {
       return CommandResult.success()
    }
}
```

* Register the command executor using the extension method for the ``PluginCommand``.

```kotlin
import com.github.shynixn.mccoroutine.suspendingExecutor

val commandSpec = CommandSpec.builder()
    .description(Text.of("Description"))
    .permission("permission.")
    .arguments(
        GenericArguments.onlyOne(GenericArguments.integer(Text.of("kills")))
    )
    .suspendingExecutor(plugin, AdminCommandExecutor())
```

##### Schedulers

* Launching a sync (Sponge Thread) scheduler.

```kotlin
import com.github.shynixn.mccoroutine.launch

val plugin : PluginContainer
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

val plugin : PluginContainer
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

val plugin : PluginContainer
val scope = plugin.scope
```

## Shipping and Running

* In order to use the MCCoroutine Api on your server, you need to put the implementation of the Api on your server.
* This can only be achieved by shipping the following dependencies with your plugin.

**Maven**
```xml
<dependency>
     <groupId>com.github.shynixn.mccoroutine</groupId>
     <artifactId>mccoroutine-sponge-core</artifactId>
     <version>0.0.5</version>
     <scope>compile</scope>
</dependency>
<dependency>
     <groupId>org.jetbrains.kotlinx</groupId>
     <artifactId>kotlinx-coroutines-core</artifactId>
     <version>1.x.x</version> 
     <scope>compile</scope>
</dependency>
<dependency>
     <groupId>org.jetbrains.kotlinx</groupId>
     <artifactId>kotlinx-coroutines-jdk8</artifactId>
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
    implementation("com.github.shynixn.mccoroutine:mccoroutine-sponge-core:0.0.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.x.x")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.x.x")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.x.x")
}
```

## Contributing

* Fork the MCCoroutine project on github and clone it to your local environment
* Install Java 8+
* Execute gradle sync for dependencies

## Licence

The source code is licensed under the MIT license. 