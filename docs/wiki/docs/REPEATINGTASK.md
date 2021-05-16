# Adding delayed and repeating tasks

This guide continues the guide 'Creating a new Plugin' and describes how delayed and repeating tasks can be performed
with coroutines.

### 1. Create MiniGame class

Create a new class which implements a custom MiniGame which allows players to join when it has not started yet.

````kotlin
class MiniGame {
    private var isStarted = false;
    private var players = HashSet<Player>()

    fun join(player: Player) {
        if (isStarted) {
            return;
        }

        players.add(player)
    }
}
````

### 2. Add a start function to the MiniGame class

Using ``delay()`` we can delay the current context (Bukkit primary thread) by 1000 milliseconds, to easily generate a
countdown without blocking the server. ``delay()`` essentially suspends the current context and continuous after the
given time.

````kotlin
class MiniGame {
    private var isStarted = false;
    private var players = HashSet<Player>()

    fun join(player: Player) {
        if (isStarted) {
            return
        }

        players.add(player)
    }

    suspend fun start() {
        if (isStarted) {
            return
        }

        isStarted = true

        for (i in 0 until 20) {
            sendMessageToPlayers("Game is starting in ${20 - i} seconds.")
            delay(1000)
        }

        // ... Teleport players to game.
    }

    private fun sendMessageToPlayers(message: String) {
        players.forEach { p -> p.sendMessage(message) }
    }
}
````

### 3. Add a run function to the MiniGame class

We can extend the start method to call ``run`` which contains a loop to tick the miniGame every 1 second.

````kotlin
class MiniGame {
    private var isStarted = false;
    private var players = HashSet<Player>()
    private var remainingTime = 0

    //...

    suspend fun start() {
        if (isStarted) {
            return
        }

        isStarted = true

        for (i in 0 until 20) {
            sendMessageToPlayers("Game is starting in ${20 - i} seconds.")
            delay(1000)
        }

        // ... Teleport players to game.
        run()
    }

    private suspend fun run() {
        remainingTime = 300 // 300 seconds

        while (isStarted && remainingTime > 0) {
            sendMessageToPlayers("Game is over in ${remainingTime} seconds.")
            delay(1000)
            remainingTime--
        }
    }

    //...
}
````

### 4. Add a function to stop the game.

An admin should be able to cancel the minigame which we implement by a ``stop`` function.

````kotlin
class MiniGame {
    private var isStarted = false;
    private var players = HashSet<Player>()
    private var remainingTime = 0

    //...

    private suspend fun run() {
        remainingTime = 300 // 300 seconds

        while (isStarted && remainingTime > 0) {
            sendMessageToPlayers("Game is over in ${remainingTime} seconds.")
            delay(1000)
            remainingTime--
        }

        if (!isStarted) {
            sendMessageToPlayers("Game was cancelled by external stop.")
        }

        isStarted = false
        // ... Teleport players back to lobby.
    }

    fun stop() {
        if (!isStarted) {
            return
        }

        isStarted = false
    }

    //...
}
````

### 5. The full MiniGame class

````kotlin
class MiniGame {
    private var isStarted = false;
    private var players = HashSet<Player>()
    private var remainingTime = 0

    fun join(player: Player) {
        if (isStarted) {
            return
        }

        players.add(player)
    }

    suspend fun start() {
        if (isStarted) {
            return
        }

        isStarted = true

        for (i in 0 until 20) {
            sendMessageToPlayers("Game is starting in ${20 - i} seconds.")
            delay(1000)
        }

        // ... Teleport players to game.
        run()
    }

    private suspend fun run() {
        remainingTime = 300 // 300 seconds

        while (isStarted && remainingTime > 0) {
            sendMessageToPlayers("Game is over in ${remainingTime} seconds.")
            delay(1000)
            remainingTime--
        }

        if (!isStarted) {
            sendMessageToPlayers("Game was cancelled by external stop.")
        }

        isStarted = false
        // ... Teleport players back to lobby.
    }

    fun stop() {
        if (!isStarted) {
            return
        }

        isStarted = false
    }

    private fun sendMessageToPlayers(message: String) {
        players.forEach { p -> p.sendMessage(message) }
    }
}
````

### 6. Connect JavaPlugin, Listener and MiniGame

````kotlin

class MiniGameListener(private val miniGame: MiniGame) : Listener {
    @EventHandler
    suspend fun onPlayerJoinEvent(playerJoinEvent: PlayerJoinEvent) {
        miniGame.join(playerJoinEvent.player)

        // Just for testing purposes
        miniGame.start()
    }
}
````

````kotlin
class MCCoroutineSamplePlugin : SuspendingJavaPlugin() {
    private val database = Database()
    private val miniGame = MiniGame()

    override suspend fun onEnableAsync() {
        database.createDbIfNotExist()
        server.pluginManager.registerSuspendingEvents(PlayerDataListener(database), plugin)
        getCommand("playerdata")!!.setSuspendingExecutor(PlayerDataCommandExecutor(database))
        server.pluginManager.registerSuspendingEvents(MiniGameListener(minigame), plugin)
    }

    override suspend fun onDisableAsync() {
    }
}
````

### 7. Test the MiniGame

Join your server to observe messages about a MiniGame being sent to you.

The next page continuous by adding caches to the plugin.
