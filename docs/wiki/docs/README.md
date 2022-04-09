# Introduction

MCCoroutine is a library, which adds extensive support for Kotlin Coroutines for Minecraft Server environments.

Plugins for game servers and proxy servers often need to perform asynchronous operations (e.g. accessing databases) to 
be scalable for a large amount of players. MCCoroutine brings the full power of Kotlin Coroutines to them by extending 
the existing APIs with suspendable commands, events and schedules.

!!! note "Kotlin Coroutines"
    Asynchronous or non-blocking programming is the new reality. Whether we're creating server-side, desktop or mobile
    applications, it's important that we provide an experience that is not only fluid from the user's perspective, but
    scalable when needed. There are many approaches to this problem, and in Kotlin we take a very flexible one by providing
    Coroutine support at the language level and delegating most of the functionality to libraries, much in line with
    Kotlin's philosophy.
    Source: (https://github.com/JetBrains/kotlin-web-site/blob/master/pages/docs/reference/coroutines-overview.md,
    Date: [09/11/2018], Licence copied to LICENCE).

**Supported Game Servers:**

* Spigot
* Paper
* CraftBukkit
* SpongeVanilla
* SpongeForge

**Supported Proxies:**

* BungeeCord

## Features

* Full implementation of Kotlin Coroutines for Minecraft Servers and Minecraft Proxies
* Extension functions for already established functions
* Connection to events, commands, schedulers
* Coroutine LifeCycle scope for plugins (supports plugin reloading)
* No NMS
* Support for Minecraft 1.7 - Latest
* Support for Java 8 - Latest
