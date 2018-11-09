# MCCoroutine

MCCoroutine is a summary of how to use the power of Kotlin corroutines on Bukkit, Spigot or Sponge Minecraft server.

## Target Audience

### What's Kotlin?

If this is your first question, please refer to the official [Kotlin] (https://kotlinlang.org/) page to get
you started. This guide deals with advanced options.

### This guide is for users who:

* Create server software for Bukkit, Spigot, Sponge, etc.
* Are already using Kotlin in their implementation.

## Getting started 

Asynchronous or non-blocking programming is the new reality. Whether we're creating server-side, desktop or mobile applications, it's important that we provide an experience that is not only fluid from the user's perspective, but scalable when needed.

There are many approaches to this problem, and in Kotlin we take a very flexible one by providing Coroutine support at the language level and delegating most of the functionality to libraries, much in line with Kotlin's philosophy.

Source:
(https://github.com/JetBrains/kotlin-web-site/blob/master/pages/docs/reference/coroutines-overview.md, Date: [09/11/2018], Licence copied to LICENCE).

## UseCase

In your server software a ordinary approach for scheduling asynchronous actions might look like this:

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

### Applying Kotlin, Extension and Functions

Let's apply some powerful syntactic sugar options Kotlin already provided in +1.2.*

This guide does not cover the details of the used functions, please refer to the following pages:

* Refer to [Functions](https://kotlinlang.org/docs/reference/functions.html, Date: [09/11/2018]).
* Refer to [Extensions](https://kotlinlang.org/docs/reference/extensions.html#extensions, Date: [09/11/2018]).

#### Extend the function of Any object

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

### Adding the magic of coroutines

You might be wondering why you should actually care about adding
coroutines to your project if the approach above is already very fancy.

The problem of the approach above is that async and sync actions can stack quite easily.
Let's simply take a look at an example.


