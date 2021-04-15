# Introduction

MCCoroutine is library which adds extensive support for Kotlin Coroutines on Minecraft Server implementing the **Bukkit-API** or **Sponge-API**.

Examples for supported frameworks:

* Spigot
* Paper
* CraftBukkit
* SpongeVanilla
* SpongeForge

!!! note "Kotlin Coroutines Description"
    Asynchronous or non-blocking programming is the new reality. Whether we're creating server-side, desktop or mobile applications, 
    it's important that we provide an experience that is not only fluid from the user's perspective, but scalable when needed.
    There are many approaches to this problem, and in Kotlin we take a very flexible one by providing Coroutine support at the language level 
    and delegating most of the functionality to libraries, much in line with Kotlin's philosophy.
    Source: (https://github.com/JetBrains/kotlin-web-site/blob/master/pages/docs/reference/coroutines-overview.md, Date: [09/11/2018], Licence copied to LICENCE).

## Features

* Full implementation of Kotlin Coroutines for Minecraft Server
* Extension functions for already established functions
* Connection to events, commands, schedulers
* Coroutine LifeCycle scope for plugins (supports plugin reloading)
* No NMS
* Support for Minecraft 1.7 - Latest
* Support for Java 8 - Latest

## Examples

* Allows appending suspend to every listener by bridging the event api with Coroutines

```kotlin
@EventHandler
suspend fun onPlayerJoinEvent(playerJoinEvent: PlayerJoinEvent) {
    val userData = withContext(plugin.asyncDispatcher) {
        Thread.sleep(500) // Simulate a database call 
        UserData()
    }
}
```

* Adds a new suspendable command executor by bridging the command api with Coroutines

```kotlin
class AdminCommandExecutor: SuspendingCommandExecutor {
    override suspend fun onCommand(sender: CommandSender,command: Command,label: String,args: Array<out String>): Boolean {
        return false
    }
}
```

* Provides its own Plugin Coroutine Scope which you can enter using new extension functions.

```kotlin
plugin.launch {
    // Coroutine Context
}
```

## Installation 

1. Add the Coroutine libraries from Jetbrains (replace 1.x.x with your Kotlin version)

**Maven**
```xml
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

```groovy
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.x.x")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.x.x")
}
```

2. Add the MCCoroutine libraries 

!!! warning
    Below you can find the dependency for the **Bukkit-API**. See the following link for
    the **Sponge-API**.

**Maven**
```xml
<dependency>
    <groupId>com.github.shynixn.mccoroutine</groupId>
    <artifactId>mccoroutine-bukkit-api</artifactId>
    <version>1.2.0</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>com.github.shynixn.mccoroutine</groupId>
    <artifactId>mccoroutine-bukkit-core</artifactId>
    <version>1.2.0</version>
    <scope>compile</scope>
</dependency>
```
**Gradle**

```groovy
dependencies {
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:1.2.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:1.2.0")
}
```
