# Introduction

```kotlin
// A new extension function 
server.pluginManager.registerSuspendingEvents(PlayerConnectListener(), plugin)
```

```kotlin
@EventHandler
suspend fun onPlayerJoinEvent(playerJoinEvent: PlayerJoinEvent) {
    val player = event.player
    // Long running operation to database is automatically suspended and continued.
    val userData = database.getUserDataFromPlayer(player)
    // Userdata was loaded asynchronous from the database and is now ready.
    println(userData.killCount)
}
```
