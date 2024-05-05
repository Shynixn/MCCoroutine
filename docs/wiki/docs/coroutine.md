# Kotlin Coroutines and Minecraft Plugins

When starting with [Coroutines in Kotlin](https://kotlinlang.org/docs/coroutines-basics.html), you may wonder how 
you can use them for minecraft plugins and mods. This guide introduces concepts and a production ready API you can use, to start
adding coroutines to your project.

!!! note "Important"
    Make sure you have already installed MCCoroutine. See [Installation](/gettingstarted) for details.

### Starting a coroutine

In order to start a coroutine, you can use the provided ``plugin.launch {}`` extension method. This is safe to be called
anywhere in your plugin except in onDisable where you need to use ``runBlocking``. However, keep in mind to **avoid** using ``runblocking`` anywhere else in any of your plugins.

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

    As Folia brings multithreading to Paper based servers, threading becomes a lot more complicated for plugin developers. 

    !!! note "Important"
        You can run mccoroutine-folia in standard Bukkit servers as well. MCCoroutine automatically falls back to the standard Bukkit 
        scheduler if the Folia schedulers are not found and the rules for mccoroutine-bukkit start to apply.
    
    !!! note "Important"
        If you have been using mccoroutine for Bukkit before, you have to perform some restructuring in your plugin. **Simply changing the imports is not enough.**
        ``plugin.launch {}`` works differently in Folia compared to Bukkit.

    First, it is important to understand that Folia does not have a server main thread. In order to access minecraft resources you need to use the correct thread for 
    a given resource. For an entity, you need to use the currently assigned thread for that entity. MCCoroutine provides dispatchers for each of these usecases in Folia
    and automatically falls back to the Bukkit dispatchers when you launch your Folia plugin on a standard Bukkit server.

    However, this does not solve the problem of accessing our own data in our plugins. We do not have a main thread, so we default on accessing our data on the incoming
    thread. However, sometimes you have to make sure only 1 thread is accessing a resource at a time. This is important for ordering events and avoiding concurrency exceptions.
    Concurrent collections can help with that but you may still need synchronize access in other places.

    As a solution, MCCoroutine proposes that each plugin gets their own "main thread" and corresponding "mainDispatcher". It is intended to execute all the stuff the plugin is going to do.
    Examples for this are retrieving and matching data like having a ``List<Game>`` or ``List<Arena>`` in minigame plugins. For minecraft actions, like teleporting a player, you start a sub context, computate the result and return it back to your personal main thread. This 
    concepts result into the following code.

    ```kotlin
    import com.github.shynixn.mccoroutine.folia.launch
    import org.bukkit.plugin.Plugin

    fun foo(entity : Entity) {
        plugin.launch { // or plugin.launch(plugin.mainDispatcher) {}
            // Your plugin main thread. If you have already been on your plugin main thread, this scope is entered immidiatly.
            // Regardless if your are on bukkit or on folia, this is your personal thread and you must not call bukkit methods on it.
            // Now perform some data access on your plugin data like accessing a repository.
            val storedEntityDataInDatabase = database.get()
            
            // Apply the data on the entity thread using the entityDispatcher.
            // The plugin.entityDispatcher(entity) parameter ensures, that we end up on the scheduler for the entity in the specific region.
            withContext(plugin.entityDispatcher(entity)) {
                  // In Folia, this will be the correct thread for the given entity.
                  // In Bukkit, this will be the main thread.
            }
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

    In Folia, MCCoroutine offers 5 custom dispatchers.

    * mainDispatcher (Your personal plugin main thread, allows to execute coroutines on it)
    * globalRegionDispatcher (Allows to execute coroutines on the global region. e.g. Global Game Rules)
    * regionDispatcher (Allows to execute coroutines on a specific location in a world)
    * entityDispatcher (Allows to execute coroutines on a specific entity)
    * asyncDispatcher (Allows to execute coroutines on the async thread pool)
    
    An example how this works is shown below:
    
    ```kotlin
    fun foo(location: Location)) {
        plugin.launch {
            // Ensures that you are now on your plugin thread.

            val resultBlockType = withContext(plugin.regionDispatcher(location)) {
                // In Folia, this will be the correct thread for the given location
                // In Bukkit, this will be the minecraft thread.
                getTypeOfBlock()
            }

            myBlockTypeList.add(resultBlockType)

            withContext(plugin.asyncDispatcher) {
                // save myBlockTypeList to file.
            }
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










