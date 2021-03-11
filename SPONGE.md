# MCCoroutine

[![Build Status](https://maven-badges.herokuapp.com/maven-central/com.github.shynixn.mccoroutine/mccoroutine-bukkit-api/badge.svg?style=flat-square
)](https://maven-badges.herokuapp.com/maven-central/com.github.shynixn.mccoroutine/mccoroutine-bukkit-api) [![GitHub license](http://img.shields.io/badge/license-MIT-blue.svg?style=flat-square)](https://raw.githubusercontent.com/Shynixn/MCCoroutine/master/LICENSE)

MCCoroutine is an extension to bukkit and sponge server implementations (Spigot, Paper, SpongeVanilla, SpongeForge etc.)
to use Kotlin Coroutines (also called async/await) pattern for all common operations.

### Table of contents

* [MCCoroutine for the Bukkit-API](https://github.com/Shynixn/MCCoroutine/blob/master/README.md)
* [MCCoroutine JavaDocs for the Bukkit-API](https://shynixn.github.io/MCCoroutine/apidocs/bukkit)
* MCCoroutine for the Sponge-API (this page)
* [MCCoroutine JavaDocs for the Sponge-API](https://shynixn.github.io/MCCoroutine/apidocs/sponge)
* [Article on how to implement Coroutines for custom frameworks]((https://github.com/Shynixn/MCCoroutine/blob/master/ARTICLE.md))

**A short listener example:**

* :exclamation: A limitation of the MCCoroutine-Implementation of Sponge is that [EventFilters](https://docs.spongepowered.org/stable/en/plugin/event/filters.html) are not working in suspending listeners. :exclamation:

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

Include the dependency to MCCoroutine.

For shipping this library with your plugin, please see ``Shipping and Running`` section below.

**Maven**

```xml

<dependency>
    <groupId>com.github.shynixn.mccoroutine</groupId>
    <artifactId>mccoroutine-sponge-api</artifactId>
    <version>1.2.0</version>
    <scope>compile</scope>
</dependency>
```

**Gradle**

```xml
dependencies {
        implementation("com.github.shynixn.mccoroutine:mccoroutine-sponge-api:1.2.0")
        }
```

**Jar File**

[MCCoroutine.jar](http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.github.shynixn.mccoroutine&a=mccoroutine-sponge-api&v=LATEST)

## Getting started

**Introduction**

Asynchronous or non-blocking programming is the new reality. Whether we're creating server-side, desktop or mobile
applications, it's important that we provide an experience that is not only fluid from the user's perspective, but
scalable when needed.

There are many approaches to this problem, and in Kotlin we take a very flexible one by providing Coroutine support at
the language level and delegating most of the functionality to libraries, much in line with Kotlin's philosophy.

Source:
(https://github.com/JetBrains/kotlin-web-site/blob/master/pages/docs/reference/coroutines-overview.md,
Date: [09/11/2018], Licence copied to LICENCE).

**Notes**

This library was created for my plugins **PetBlocks** and **BlockBall** as I had to deal with sometimes short, sometimes
long running async operations. Caching database results is an essential component of both and a cache miss should not
block the server in any way.

## Code Examples

* If you need more information, check out the sample implementation of a plugin using MCCoroutine in
  the `mccoroutine-sponge-sample` folder.
* In detail coroutines work with context switches but for simplification cases this guide uses the
  term `async operations` instead of `context switches`.

### Event Listener, Commands, Schedulers

##### Registering a suspending event listener

* Create an ordinary listener and simple prepend ``suspend`` to all functions where you are going to perform async
  operations. If you do not perform async operations, you do not need to append it.

```kotlin
class PlayerConnectListener {
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

* If you send a custom event to a suspending listener, it may complete in the future and it becomes impossible to check
  for the result. However, the extension function below allows to get a collection of awaitable jobs after firing a
  single event.

```kotlin
Plugin plugin
        Event event
val jobs = Sponge.getEventManager().postSuspending(event, pluginContainer)
jobs.joinAll()
```

##### Registering a suspending command executor

* If you need to perform async operations, implement the SuspendingCommandExecutor instead of the standard command
  executor.

```kotlin
import com.github.shynixn.mccoroutine.SuspendingCommandExecutor

class AdminCommandExecutor : SuspendingCommandExecutor {
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

* Adding tab completion for a custom ``CommandElement``

```kotlin
class SetCommandElement(pluginContainer: PluginContainer, text: Text) :
    SuspendingCommandElement(pluginContainer, text) {
    /**
     * Attempt to extract a value for this element from the given arguments.
     * This method is expected to have no side-effects for the source, meaning
     * that executing it will not change the state of the [CommandSource]
     * in any way.
     *
     * @param source The source to parse for
     * @param args the arguments
     * @return The extracted value
     * @throws ArgumentParseException if unable to extract a value
     */
    override suspend fun parseValue(source: CommandSource, args: CommandArgs): Any? {
        val value = args.next()

        if (value.equals("set", true)) {
            return "set"
        }

        args.createError(Text.of("Input $value is not 'set'."))
        return null
    }

    /**
     * Fetch completions for command arguments.
     *
     * @param src The source requesting tab completions
     * @param args The arguments currently provided
     * @param context The context to store state in
     * @return Any relevant completions
     */
    override suspend fun complete(src: CommandSource, args: CommandArgs, context: CommandContext): List<String?>? {
        return listOf("set")
    }
}
```

```kotlin
import com.github.shynixn.mccoroutine.suspendingExecutor

val commandSpec = CommandSpec.builder()
    .description(Text.of("Description"))
    .permission("permission.")
    .arguments(
        GenericArguments.onlyOne(
            SetCommandElement(plugin, Text.of("action")).toCommandElement()
        ),
        GenericArguments.onlyOne(GenericArguments.integer(Text.of("kills")))
    )
    .suspendingExecutor(plugin, AdminCommandExecutor())
```

##### Schedulers

* Launching a sync (Sponge Thread) scheduler.

```kotlin
import com.github.shynixn.mccoroutine.launch

val plugin: PluginContainer
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

val plugin: PluginContainer
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

val plugin: PluginContainer
val scope = plugin.scope
```

## Shipping and Running

* In order to use the MCCoroutine Api on your server, you need to put the implementation of the Api on your server.
* This can only be achieved by shipping the following dependencies with your plugin.

**Maven**

```xml

<dependency>
    <groupId>com.github.shynixn.mccoroutine</groupId>
    <artifactId>mccoroutine-sponge-api</artifactId>
    <version>1.2.0</version>
    <scope>compile</scope>
</dependency>
<dependency>
<groupId>com.github.shynixn.mccoroutine</groupId>
<artifactId>mccoroutine-sponge-core</artifactId>
<version>1.2.0</version>
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
```

**Gradle**

```xml
dependencies {
        implementation("com.github.shynixn.mccoroutine:mccoroutine-sponge-api:1.2.0")
        implementation("com.github.shynixn.mccoroutine:mccoroutine-sponge-core:1.2.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.x.x")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.x.x")
        }
```

## Contributing

* Fork the MCCoroutine project on github and clone it to your local environment
* Install Java 8+
* Execute gradle sync for dependencies

## Licence

The source code is licensed under the MIT license. 
