# Kotlin Coroutines and Minecraft Plugins

When starting with [Coroutines in Kotlin](https://kotlinlang.org/docs/coroutines-basics.html), it is interesting
how this can be translated to the world of minecraft plugins. It is recommended to learn how Kotlin Coroutines work
before you continue here.

!!! note "Important"
    Make sure you have already installed MCCoroutine. See [Installation](/gettingstarted) for details.

### Starting a coroutine

For beginners, it is often confusing how to enter a coroutine. The examples in the official guide mostly
use ``runBlocking``
because it makes sense for testing. However, keep in mind to **avoid** using ``runblocking`` in any of your plugins.

* To enter a coroutine **anywhere** in your code at any time:

=== "Bukkit"

    ```kotlin
    import com.github.shynixn.mccoroutine.bukkit.launch
    import org.bukkit.plugin.Plugin

    fun foo() {
        plugin.launch {
            // This will always be on the minecraft main thread.
        }
    }
    ```

=== "BungeeCord"

    ```kotlin
    import com.github.shynixn.mccoroutine.bungeecord.launch
    import net.md_5.bungee.api.plugin.Plugin

    fun foo() {
        plugin.launch {
            // This will always be on the minecraft main thread.
        }
    }
    ```

=== "Sponge"

    ```kotlin
    import com.github.shynixn.mccoroutine.sponge.launch
    import org.spongepowered.api.plugin.PluginContainer

    fun foo() {
        plugin.launch {
            // This will always be on the minecraft main thread.
        }
    }
    ```

=== "Velocity"

    ```kotlin
    import com.github.shynixn.mccoroutine.velocity.launch
    import com.velocitypowered.api.plugin.PluginContainer

    fun foo() {
        plugin.launch {
            // This will always be on the minecraft main thread.
        }
    }
    ```

=== "Minestom"

    Minestom has got 2 lifecycle scopes, the server scope and the extension scope.
    When this guide talks about a ``plugin``, the corresponding class in Minestom is ``Extension`` or ``MinecraftServer`` depending on your usecase.

    Server level (if you are developing a new server):

    ```kotlin
    import com.github.shynixn.mccoroutine.minestom.launch
    import net.minestom.server.MinecraftServer

    fun foo() {
        server.launch {
            // This will always be on the minecraft main thread.
        }
    }
    ```   

    Extension level (if you are developing a new extension): 

    ```kotlin
    import com.github.shynixn.mccoroutine.minestom.launch
    import net.minestom.server.extensions.Extension

    fun foo() {
        extension.launch {
            // This will always be on the minecraft main thread.
        }
    }
    ```   


### Switching coroutine context

Later in the [Coroutines in Kotlin](https://kotlinlang.org/docs/coroutine-context-and-dispatchers.html) guide, the terms
coroutine-context and dispatchers are explained.
A dispatcher determines what thread or threads the corresponding coroutine uses for its execution. Therefore,
MCCoroutine offers 2 custom dispatchers:

* minecraftDispatcher (Allows to execute coroutines on the main minecraft thread)
* asyncDispatcher (Allows to execute coroutines on the async minecraft threadpool)

!!! note "Important"
    **However, it is highly recommend to use ``Dispatchers.IO`` instead of asyncDispatcher because the scheduling is more
    accurate.**
    Additional technical details can be found here: [GitHub Issue](https://github.com/Shynixn/MCCoroutine/issues/87).

An example how this works is shown below:

```kotlin
fun foo() {
    plugin.launch {
        // This will always be on the minecraft main thread.

        val result1 = withContext(plugin.minecraftDispatcher) {
            // Perform operations on the minecraft main thread.
            "Player is " // Optionally, return a result.
        }

        // Here we are automatically back on the main thread again.

        // Prefer using Dispatchers.IO instead of asyncDispatcher 
        val result2 = withContext(Dispatchers.IO) {
            // Perform operations asynchronously.
            " Max"
        }

        // Here we are automatically back on the main thread again.

        println(result1 + result2) // Prints 'Player is Max'
    }
}
```

Normally, you do not need to call ``plugin.minecraftDispatcher`` in your code. Instead, you are guaranteed to be always
on the minecraft main thread
in the ``plugin.launch{}`` scope and use sub coroutines (e.g. withContext) to perform asynchronous operations. Such a
case can be found below:

```kotlin
// This is a Bukkit example, but it works in the same way in every other framework.
@EventHandler
fun onPlayerJoinEvent(event: PlayerJoinEvent) {
    plugin.launch {
        // Main Thread
        val name = event.player.name
        val listOfFriends = withContext(Dispatchers.IO) {
            // IO Thread
            val friendNames = Files.readAllLines(Paths.get("$name.txt"))
            friendNames
        }

        // Main Thread
        val friendText = listOfFriends.joinToString(", ")
        event.player.sendMessage("My friends are: $friendText")
    }
}

```

### Plugin launch Execution order

If you use ``plugin.launch``, it is important to understand the execution order.

````kotlin
// This is a Bukkit example, but it works in the same way in every other framework.
class Foo(private val plugin: Plugin) {

    fun bar() {
        // Main Thread
        println("I am first")

        val job = plugin.launch {
            println("I am second") // The context is not suspended when switching to the same suspendable context.
            delay(1000)
            println("I am fourth") // The context is given back after 1000 milliseconds and continuous here.
            bob()
        }

        // When calling delay the suspendable context is suspended and the original context immediately continuous here.
        println("I am third")
    }

    private suspend fun bob() {
        println("I am fifth")
    }
}
````

````kotlin
"I am first"
"I am second"
"I am third"
"I am fourth"
"I am fifth"
````

### Coroutines everywhere

Using ``plugin.launch{}``is valuable if you migrate existing plugins to use coroutines. However, if you write a new
plugin from scratch, you may consider using
convenience integrations provided by MCCoroutine such as:

* Suspending Plugin
* Suspending Listeners
* Suspending CommandExecutors










