# Suspending Delayed, Repeating Tasks

This page explains how you can delay and repeat tasks using Kotlin Coroutines.

## Delaying tasks

If you are already in a ``suspend`` function, you can simply use ``delay`` to delay an execution.

Using ``delay`` we can delay the current context (e.g. Main Thread) by some milliseconds, to easily delay actions
without blocking the server. ``delay`` essentially suspends the current context and continuous after the
given time.

!!! note "Difference between delay() and Thread.sleep()"
    There is a big difference with ``delay()`` and ``Thread.sleep()``. Consult the official Kotlin Coroutines
    documentation for details, however essentially ``Thread.sleep()`` blocks the thread for a given time and
    ``delay()`` suspends the thread for a given time. When a thread is suspended, it can do other work (e.g. server handles
    other operations like players joining or commands) compared to when a thread is blocked, it cannot do other work (e.g.  server appears frozen).

````kotlin
suspend fun sayHello() {
    println("Please say hello in 2 seconds")
    delay(2000) // Delay for 2000 milliseconds
    println("hello")
}
````

If you are not in a ``suspend`` function, use ``plugin.launch`` together with ``delay``.

````kotlin
fun sayHello() {
    plugin.launch {
        println("Please say hello in 2 seconds")
        delay(2000) // Delay for 2000 milliseconds
        println("hello")
    }
}
````

## Delay Ticks

MCCoroutine offers an extension method to use delay together with Bukkit and Sponge ticks.

```kotlin
delay(1.ticks)
```

Prefer using ``delay(1.ticks)`` when delaying on the minecraft main thread instead of ``delay(50)``. The tick extension function is more accurate than using
milliseconds directly. The technical details are explained in this [github issue](https://github.com/Shynixn/MCCoroutine/issues/72).

## Repeating tasks

If you are already in a ``suspend`` function, you can simply use traditional loops with ``delay`` to repeat tasks.

````kotlin
suspend fun sayHello() {
    println("Please say hello 10 times every 2 seconds")

    for (i in 0 until 10) {
        delay(2000) // Delay for 2000 milliseconds
        println("hello")
    }
}
````

If you are not in a ``suspend`` function, use ``plugin.launch`` together with ``delay``.

````kotlin
fun sayHello() {
    plugin.launch {
        println("Please say hello 10 times every 2 seconds")

        for (i in 0 until 10) {
            delay(2000) // Delay for 2000 milliseconds
            println("hello")
        }
    }
}
````

## Creating a Minigame using delay (Bukkit)

One example where ``delay`` is really useful is when creating minigames. It makes the
contract of minigame classes very easy to understand. Let's start by implementing a basic minigame class.

The first example shows a countdown in the start function of the minigame.

````kotlin
import kotlinx.coroutines.delay
import org.bukkit.entity.Player

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

        // This loop represents a traditional repeating task which ticks every 1 second and is called 20 times.
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

### Add a run function to the MiniGame class

We can extend the start method to call ``run`` which contains a loop to tick the miniGame every 1 second.

````kotlin
import kotlinx.coroutines.delay
import org.bukkit.entity.Player

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

        // This loop represents a traditional repeating task which ticks every 1 second and is called 20 times.
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

### Add a function to stop the game.

An admin should be able to cancel the minigame, which we can implement by a ``stop`` function.

````kotlin
import kotlinx.coroutines.delay
import org.bukkit.entity.Player

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

### The full MiniGame class:

````kotlin
import kotlinx.coroutines.delay
import org.bukkit.entity.Player

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

        // This loop represents a traditional repeating task which ticks every 1 second and is called 20 times.
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

### Connect JavaPlugin, Listener and MiniGame

````kotlin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

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
import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.github.shynixn.mccoroutine.bukkit.setSuspendingExecutor

class MCCoroutineSamplePlugin : SuspendingJavaPlugin() {
    private val database = Database()
    private val miniGame = MiniGame()

    override suspend fun onEnableAsync() {
        // Minecraft Main Thread
        database.createDbIfNotExist()
        server.pluginManager.registerSuspendingEvents(PlayerDataListener(database), this)
        getCommand("playerdata")!!.setSuspendingExecutor(PlayerDataCommandExecutor(database))
        server.pluginManager.registerSuspendingEvents(MiniGameListener(miniGame), this)
    }

    override suspend fun onDisableAsync() {
        // Minecraft Main Thread
    }
}
````

### Test the MiniGame

Join your server to observe Minigame messages getting printed to your server log.
