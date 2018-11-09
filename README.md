# MCCoroutine

MCCoroutine is a summary of how to use the power of Kotlin corroutines on Bukkit, Spigot or Sponge Minecraft server.

## Contents

* [Getting started](https://github.com/Shynixn/MCCoroutine#getting-started)
* [UseCase](https://github.com/Shynixn/MCCoroutine#UseCase)
* [Adding coroutines to your project](https://github.com/Shynixn/MCCoroutine#Adding-coroutines-to-your-project)
* [Using coroutines in your project](https://github.com/Shynixn/MCCoroutine#Using-coroutines-in-your-project)

## Target Audience

### What's Kotlin?

If this is your first question, please refer to the official [Kotlin](https://kotlinlang.org/) page to get
you started. 

### This guide is for users who:

* Create server software for Bukkit, Spigot, Sponge, etc.
* Are already using Kotlin in their implementation.

## Getting started 

Asynchronous or non-blocking programming is the new reality. Whether we're creating server-side, desktop or mobile applications, it's important that we provide an experience that is not only fluid from the user's perspective, but scalable when needed.

There are many approaches to this problem, and in Kotlin we take a very flexible one by providing Coroutine support at the language level and delegating most of the functionality to libraries, much in line with Kotlin's philosophy.

Source:
(https://github.com/JetBrains/kotlin-web-site/blob/master/pages/docs/reference/coroutines-overview.md, Date: [09/11/2018], Licence copied to LICENCE).

## UseCase

In your server software, an ordinary approach for scheduling asynchronous actions might look like this:

#### Java - Bukkit
```java
private Plugin plugin; // Initialized plugin.

@EventHandler
public void onPlayerInteractEvent(PlayerInteractEvent event) {
    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
        List<String> data = getDataFromDatabase();
        
        plugin.getServer().getScheduler().runTask(plugin, () -> {
                
            if(data.contains("give-apple")) {
                event.getPlayer().getInventory().addItem(new ItemStack(Material.APPLE))
            }
        });
    });
}
```

or this 

#### Java - Sponge
```java
private PluginContainer pluginContainer;

@Listener
public void onPlayerInteractEvent(HandInteractEvent event, @First(typeFilter = Player.class) Player player){
    Task.builder().async().execute(() -> {
        List<String> data = getDataFromDatabase();

        Task.builder().execute(() -> {
            
            if(data.contains("give-apple")) {
                player.getInventory().offer(ItemStack.builder().itemType(ItemTypes.APPLE).build());
            }
        }).submit(pluginContainer);
    }).submit(pluginContainer);
}
```

Both implementations cover the same usecase, however it is not that easy to guess what is happening.

Let's quickly go over to the Kotlin part, because it is getting smelly in here.

## Applying Kotlin, Extension and Functions

Let's apply some powerful syntactic sugar options Kotlin already provided in +1.2.*

This guide does not cover the details of the used functions, please refer to the following pages:

* Refer to [Functions](https://kotlinlang.org/docs/reference/functions.html). Date: [09/11/2018]
* Refer to [Extensions](https://kotlinlang.org/docs/reference/extensions.html).  Date: [09/11/2018]

### Extend the function of Any object

##### Bukkit

This extension allows switching to a async task with optional delaying or repeating.

```kotlin
/**
 * Executes the given [f] via the [plugin] asynchronous.
 */
fun Any.async(plugin : Plugin, delayTicks: Long = 0L, repeatingTicks: Long = 0L, f: () -> Unit) {
    if (repeatingTicks > 0) {
        plugin.server.scheduler.runTaskTimerAsynchronously(plugin, f, delayTicks, repeatingTicks)
    } else {
        plugin.server.scheduler.runTaskLaterAsynchronously(plugin, f, delayTicks)
    }
}
```

Add a similar implementation for a sync task.

```kotlin
/**
 * Executes the given [f] via the [plugin] synchronized with the server tick.
 */
fun Any.sync(plugin : Plugin, delayTicks: Long = 0L, repeatingTicks: Long = 0L, f: () -> Unit) {
    if (repeatingTicks > 0) {
        plugin.server.scheduler.runTaskTimer(plugin, f, delayTicks, repeatingTicks)
    } else {
        plugin.server.scheduler.runTaskLater(plugin, f, delayTicks)
    }
}
```

This 2 methods should be defined only once global in your project. However, let us take a look into 
the actual useage.

```kotlin
private val plugin : Plugin

@EventHandler
fun onPlayerInteractEvent(event: PlayerInteractEvent) {
    async(plugin){
        val data = getDataFromDatabase()

        sync(plugin){
            if (data.contains("give-apple")) {
                event.player.inventory.addItem(ItemStack(Material.APPLE))
            }
        }
    }
}
 ```   

##### Sponge

This extension allows switching to a async task with optional delaying or repeating.

```kotlin
/**
 * Executes the given [f] via the [plugin] asynchronous.
 */
fun Any.async(plugin: PluginContainer, delayTicks: Long = 0L, repeatingTicks: Long = 0L, f: () -> Unit) {
    val builder = Task.builder().async().execute(f).delayTicks(delayTicks)

    if (repeatingTicks > 0) {
        builder.intervalTicks(repeatingTicks)
    }

    builder.submit(plugin)
}
```

Add a similar implementation for a sync task.

```kotlin
/**
 * Executes the given [f] via the [plugin] synchronized with the server tick.
 */
fun Any.async(plugin: PluginContainer, delayTicks: Long = 0L, repeatingTicks: Long = 0L, f: () -> Unit) {
    val builder = Task.builder().execute(f).delayTicks(delayTicks)

    if (repeatingTicks > 0) {
        builder.intervalTicks(repeatingTicks)
    }

    builder.submit(plugin)
}
```

This 2 methods should be defined only once global in your project. However, let us take a look into 
the actual useage.

```kotlin
private val pluginContainer: PluginContainer
    
@Listener
fun onPlayerInteractEvent(event: HandInteractEvent, @First(typeFilter = [Player::class]) player: Player) {
    async(pluginContainer) {
        val data = getDataFromDatabase()

        sync(pluginContainer) {
            if (data.contains("give-apple")) {
                player.inventory.offer(ItemStack.builder().itemType(ItemTypes.APPLE).build())
            }
        }
    }
}
 ```   

## Adding coroutines to your project

### Motivation

You might be wondering why you should actually care about adding
coroutines to your project if the approach above is already very fancy.

The problem of the approach above is that async and sync actions can stack quite easily.
Let's simply take a look at an example.

```kotlin
private val pluginContainer: PluginContainer
    
@Listener
fun onPlayerInteractEvent(event: HandInteractEvent, @First(typeFilter = [Player::class]) player: Player) {
    async(plugin){
       val data = getDataFromDatabase()

       sync(plugin){
           if (data.contains("give-apple")) {
               giveAppleToPlayer()

               async(plugin){
                   val appleConfiguration = getAppleConfigurationFromFile()

                   sync(plugin){
                       if(appleConfiguration.contains("apple-lifetime")){
                           applyLifeTimeToApple()
                       }
                   }
               }
           }
       }
   }
}
 ```   
 
Of course, this could be simplified but you are getting the point.  
Also, parallel tasks are still lots of work to accomplish.

### Implementing

#### Creating a Dispatcher (Bukkit)

The first problem we need to solve in order use coroutines is letting Kotlin know how our custom framework handles
threads otherwise it does not know how to schedule our tasks correctly on each thread. 

This may sound difficult but has become very easy to implement in Kotlin +1.3.

There are some default dispatchers available for Android or JavaFX, however there is not one for Bukkit, Spigot or Sponge.

When taking a look at the JavaFX implementation which is most similar to our use case we can come up with the following.

Add a dispatcher for the main thread.

```kotlin
class MinecraftCoroutineDispatcher(private val plugin: Plugin) : CoroutineDispatcher() {
    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (Bukkit.isPrimaryThread()) {
            block.run()
        } else {
            plugin.server.scheduler.runTask(plugin, block)
        }
    }
}
 ``` 
 
Add a dispatcher for async threads.

```kotlin
class AsyncCoroutineDispatcher(private val plugin: Plugin) : CoroutineDispatcher() {
    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (Bukkit.isPrimaryThread()) {
            plugin.server.scheduler.runTaskAsynchronously(plugin, block)
        } else {
            block.run()
        }
    }
}
 ``` 
 
Now we need a place where to store the instances of the dispatchers. The following code snippet is only an example and should be
replace with more clean approaches.
 
```kotlin
object DispatcherContainer {
    private var asyncCoroutine: CoroutineContext? = null
    private var syncCoroutine: CoroutineContext? = null

    /**
     * Gets the async coroutine context.
     */
    val async: CoroutineContext
        get() {
            if (asyncCoroutine == null) {
                asyncCoroutine = AsyncCoroutineDispatcher(JavaPlugin.getPlugin(YourPlugin::class.java))
            }

            return asyncCoroutine!!
        }

    /**
     * Gets the sync coroutine context.
     */
    val sync: CoroutineContext
        get() {
            if (syncCoroutine == null) {
                syncCoroutine = MinecraftCoroutineDispatcher(JavaPlugin.getPlugin(YourPlugin::class.java))
            }

            return syncCoroutine!!
        }
}
 ``` 

You might have noticed that some overhead is required in order to add coroutines to your solution. 

This means you should keep in mind that smaller projects should stay with the discussed extensions approach instead of adding this powerful framework.

#### Adding syntactic sugar

After adding these 3 classes you are already done, however adding some syntactic sugar is always nice.

Define the following code globally.

```kotlin
/**
 * Minecraft async dispatcher.
 */
val Dispatchers.async: CoroutineContext
    get() =  DispatcherContainer.async

/**
 * Minecraft sync dispatcher.
 */
val Dispatchers.minecraft: CoroutineContext
    get() =  DispatcherContainer.sync
 ``` 


## Using coroutines in your project

It is highly recommend to copy the following snippets into a event method in order to get a feeling how
the actions flow together.

**All samples below do NOT block the main thread!**

```kotlin
@EventHandler
fun onPlayerInteractEvent(event: PlayerInteractEvent) {
    println("Event on Thread " + Thread.currentThread().id)

    // Launch always starts a coroutine.
    GlobalScope.launch(Dispatchers.minecraft) {
        println("Launch-1 on Thread " + Thread.currentThread().id)

        // Use withContext for dispatching async operations.
        val data = withContext(Dispatchers.async) {
            Thread.sleep(1000)
            println("Async-1-Database on Thread " + Thread.currentThread().id)
            arrayListOf("give-apple")
        }

        // Once the data is here, continue on the main thread.
        println("Launch-2 on Thread " + Thread.currentThread().id)
    }
 }
 
 // Execution Order: Event, Launch-1, Async-1, Launch-2
 ``` 
 
```kotlin
@EventHandler
fun onPlayerInteractEvent(event: PlayerInteractEvent) {
    println("Event on Thread " + Thread.currentThread().id)
    
    // Launch always starts a coroutine.
    GlobalScope.launch(Dispatchers.minecraft) {
        println("Launch-1 on Thread " + Thread.currentThread().id)

        // Use withContext for dispatching async operations.
        val data = withContext(Dispatchers.async) {
            Thread.sleep(1000)
            println("Async-1-Database on Thread " + Thread.currentThread().id)
            arrayListOf("give-apple")
        }

        // Once the data is here, continue on the main thread.
        println("Launch-2 on Thread " + Thread.currentThread().id)

        if (data.contains("give-apple")) {
            println("Launch-3 on Thread " + Thread.currentThread().id)
            val currentMilliSeconds = System.currentTimeMillis()

            // Use async for parallel async operations.
            val appleConfigTask = async(Dispatchers.async) {
                Thread.sleep(2000)
                println("Async-2-Config on Thread " + Thread.currentThread().id)
                arrayListOf("life-time")
            }

            // Use async for parallel async operations.
            val appleDefinitionTask = async(Dispatchers.async) {
                Thread.sleep(3000)
                println("Async-3-Definition on Thread " + Thread.currentThread().id)
                arrayListOf("definition")
            }

            println("Launch-4 on Thread " + Thread.currentThread().id)

            // Wait until both tasks are finished. 
            // This does not block the main thread either.
            val appleConfig = appleConfigTask.await()
            val appleDefinition = appleDefinitionTask.await()

            // As both tasks get executed parallel, it only took ~3 seconds instead of ~5 seconds to finish them.
            val milliSeconds = System.currentTimeMillis() - currentMilliSeconds
            println("Launch-5 on Thread " + Thread.currentThread().id + " after " + milliSeconds + ".")
            
            if (appleConfig.contains("life-time") && appleDefinition.contains("definition")) {
                println("Good job! You have understood coroutines!")
            }
        }
    }
 }
 
 // Execution Order: Event, Launch-1, Async-1, Launch-2, Launch-3, Launch-4, Async-2, Async-3, Launch-5, Good job!
 ``` 

For further information, please refer to [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) for the official documentation. 
