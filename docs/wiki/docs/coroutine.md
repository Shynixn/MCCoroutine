# Kotlin Coroutines and Minecraft Plugins

When starting with [Coroutines in Kotlin](https://kotlinlang.org/docs/coroutines-basics.html), it is interesting
how this can be translated to the world of minecraft plugins. It is recommended to learn how Kotlin Coroutines work
before you continue here.

!!! note "Important"
    Make sure you have already installed MCCoroutine. See [Installation](/gettingstarted) for details.

### Starting a coroutine

In order to start coroutine You may also encounter the function
``runBlocking`` because it makes sense for certain scenarios such as unittest.
However, keep in mind to **avoid** using ``runblocking`` in any of your plugins.

* To enter a coroutine **anywhere** in your code at any time:

=== "Bukkit"

    ```kotlin
    import com.github.shynixn.mccoroutine.bukkit.launch
    import org.bukkit.plugin.Plugin

    fun foo() {
        plugin.launch {
            // This will always be on the minecraft main thread.
            // If you have been on the minecraft main thread before calling plugin.launch, this scope is entered immediately without any delay.
        }
    }
    ```

=== "BungeeCord"

    ```kotlin
    import com.github.shynixn.mccoroutine.bungeecord.launch
    import net.md_5.bungee.api.plugin.Plugin

    fun foo() {
        plugin.launch {
            // This a random thread on the bungeeCord threadPool.
            // If you have been on the bungeeCord threadPool before calling plugin.launch, this scope is executed in the next scheduler tick.
            // If you pass CoroutineStart.UNDISPATCHED, you can enter this scope in the current tick. This is shown in a code example below.
        }
    }
    ```

=== "Fabric"

    Fabric has got 3 lifecycle scopes, the ``ModInitializer`` (both client and server) ``ClientModInitializer`` (client) and ``DedicatedServerModInitializer`` scope.
    This guide gives only ``DedicatedServerModInitializer`` examples but it works in the same way for the other scopes.

    ```kotlin
    import com.github.shynixn.mccoroutine.fabric.launch
    import net.fabricmc.api.DedicatedServerModInitializer

    fun foo(){
        mod.launch {
            // This will always be on the minecraft main thread.
        }
    }
    ```

=== "Folia"

    As Folia brings multithreading to Paper based servers, threading becomes more complicated and MCCoroutine requires you to think 
    everytime you call plugin.launch. In Bukkit based servers, MCCoroutine can assume the correct thread automatically and optimise ticking. (e.g.
    not sending a task to the scheduler if you are already on the main thread). 

    In Folia, there are many threadpools (explained below) and we do not have a main thread.

    !!! note "Important"
        You can run mccoroutine-folia in standard Bukkit servers as well. MCCoroutine automatically falls back to the standard Bukkit 
        scheduler if the Folia schedulers are not found and the rules for mccoroutine-bukkit start to apply.

    ```kotlin
    import com.github.shynixn.mccoroutine.folia.launch
    import org.bukkit.plugin.Plugin

    fun foo(entity : Entity) {
        // The plugin.entityDispatcher(entity) parameter ensures, that we end up on the scheduler for the entity in the specific region if we suspend
        // inside the plugin.launch scope. (e.g. using delay)
        // The CoroutineStart.UNDISPATCHED ensures, that we enter plugin.launch scope without any delay on the current thread. 
        // You are responsible to ensure that you are on the correct thread pool (in this case the thread pool for the entity), if you pass CoroutineStart.UNDISPATCHED.
        // This is automatically the case if you use plugin.launch{} in events or commands. You can simply use CoroutineStart.UNDISPATCHED here.
        // If you use CoroutineStart.DEFAULT, the plugin.launch scope is entered in the next scheduler tick.
        plugin.launch(plugin.entityDispatcher(entity), CoroutineStart.UNDISPATCHED) {
            // In this case this will be the correct thread for the given entity, if the thread was correct before calling plugin.launch.
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
            // This will be a random thread on the Velocity threadpool
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
A dispatcher determines what thread or threads the corresponding coroutine uses for its execution.

=== "Bukkit"

    In Bukkit, MCCoroutine offers 2 custom dispatchers.

    * minecraftDispatcher (Allows to execute coroutines on the main minecraft thread)
    * asyncDispatcher (Allows to execute coroutines on the async minecraft threadpool)

    !!! note "Important"
        You may also use ``Dispatchers.IO`` instead of asyncDispatcher, to reduce the dependency on mccoroutine in your code.
    
    An example how this works is shown below:
    
    ```kotlin
    fun foo() {
        plugin.launch {
            // This will always be on the minecraft main thread.
            // If you have been on the minecraft main thread before calling plugin.launch, this scope is entered immediately without any delay.
    
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
            // This will always be on the minecraft main thread.
            // A PlayerJoinEvent arrives on the main thread, therefore this scope is entered immediately without any delay.

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
            // If you have been on the minecraft main thread before calling plugin.launch, this scope is entered immediately without any delay.
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

=== "BungeeCord"

    In BungeeCord, MCCoroutine offers 1 custom dispatcher.

    * bungeeCordDispatcher (Allows to execute coroutines on the bungeeCord threadpool)
    
    An example how this works is shown below:
    
    ```kotlin
    fun foo() {
        plugin.launch {
            // This a random thread on the bungeeCord threadPool.
            // If you have been on the bungeeCord threadPool before calling plugin.launch, this scope is executed in the next scheduler tick.
            // If you pass CoroutineStart.UNDISPATCHED, you can enter this scope in the current tick. This is shown in a code example below.
    
            val result = withContext(Dispatchers.IO) {
                  // Perform operations asynchronously.
                "Playxer is Max"
            }
    
            // Here we are automatically back on a new random thread on the bungeeCord threadPool.
            println(result) // Prints 'Player is Max'
        }
    }
    ```

    ```kotlin
    fun foo() {
        plugin.launch(start = CoroutineStart.UNDISPATCHED) {
            // This is the same thread before calling plugin.launch

            val result = withContext(Dispatchers.IO) {
                // Perform operations asynchronously.
                "Playxer is Max"
            }

            // Here we are automatically back on a new random thread on the bungeeCord threadPool.
            println(result) // Prints 'Player is Max'
        }
    }
    ```

=== "Fabric"

    TBD

=== "Folia"

    In Folia, MCCoroutine offers 4 custom dispatchers.

    * globalRegion (Allows to execute coroutines on the global region. e.g. Global Game Rules)
    * regionDispatcher (Allows to execute coroutines on a specific location in a world)
    * entityDispatcher (Allows to execute coroutines on a specific entity)
    * asyncDispatcher (Allows to execute coroutines on the async thread pool)
    
    An example how this works is shown below:
    
    ```kotlin
    fun foo(location: Location)) {
        plugin.launch(plugin.regionDispatcher(location), CoroutineStart.UNDISPATCHED) {
            // The correct thread for the given location without delay, if the thread was correct before calling plugin.launch.

            val result = withContext(Dispatchers.IO) {
                  // Perform operations asynchronously.
                "Playxer is Max"
            }
    
           // The correct thread for the given location.
            println(result) // Prints 'Player is Max'
        }
    }
    ```

=== "Sponge"

    TBD

=== "Velocity"

    TBD

=== "Minestom"

    TBD

### Coroutines everywhere

Using ``plugin.launch{}``is valuable if you migrate existing plugins to use coroutines. However, if you write a new
plugin from scratch, you may consider using
convenience integrations provided by MCCoroutine such as:

* Suspending Plugin
* Suspending Listeners
* Suspending CommandExecutors










