# Coroutines in onDisable

(This site is only relevant for Spigot, CraftBukkit and Paper)

After moving most of your code to suspend functions, you may want to launch a coroutine in the ``onDisable`` or
any other function, which gets called, after the plugin has already been disabled.

## Default Behaviour (ShutdownStrategy=Scheduler)

The default behaviour of MCCoroutine is to stop all coroutines immediately, once the ``BukkitScheduler`` has been
shutdown. This happens automatically and **before** your ``onDisable`` function of your ``JavaPlugin`` class
gets called.

If you try the following, you run into the following exception.

```kotlin
override fun onDisable() {
    println("[onDisable] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")
    val plugin = this

    plugin.launch {
        println("[onDisable] Simulating data save on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")
        Thread.sleep(500)
    }

    println("[onDisable] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")
}
```

```
java.lang.RuntimeException: Plugin MCCoroutine-Sample attempted to start a new coroutine session while being disabled.
```

This behaviour makes sense, because the BukkitScheduler works in the same way. MCCoroutine is just a smart wrapper for it.

#### Calling a suspend function

However, you may **have to call** a suspend function anyway. This one of the few exceptions were using ``runBlocking`` makes
sense:

```kotlin
override fun onDisable() {
    println("[onDisable] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")
    val plugin = this

    runBlocking {
        foo()
    }

    println("[onDisable] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")
}

suspend fun foo() {
    println("[onDisable] Simulating data save on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")
    Thread.sleep(500)
}
```

## Manual Behaviour (ShutdownStrategy=Manual)

The default strategy is the recommend one and you should design your plugin according that.

However, there may be edge cases, where
you need full control over handling remaining coroutine jobs and use ```minecraftDispatcher``` or ``asyncDispatcher``
after the plugin has been disabled.

Change the shutdownStrategy in ``onEnable``

```kotlin
override fun onEnable() {
    val plugin = this
    plugin.mcCoroutineConfiguration.shutdownStrategy = ShutdownStrategy.MANUAL

    // Your code ...
}
```

Call ``disposePluginSession`` after you are finished.

```kotlin
override fun onDisable() {
    // Your code ...

    val plugin = this
    plugin.mcCoroutineConfiguration.disposePluginSession()
}
```

### Plugin.launch is back

This allows to use ``plugin.launch`` in your ``onDisable`` function.

```kotlin
override fun onDisable() {
    val plugin = this
    println("[MCCoroutineSamplePlugin/onDisableAsync] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")

    plugin.launch {
        println("[MCCoroutineSamplePlugin/onDisableAsync] Number 1:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")
        delay(500)
        println("[MCCoroutineSamplePlugin/onDisableAsync] Number 2:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")
    }

    plugin.mcCoroutineConfiguration.disposePluginSession()
    println("[MCCoroutineSamplePlugin/onDisableAsync] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")
}
```

```
[Server thread/INFO]: [MCCoroutine-Sample] Disabling MCCoroutine-Sample
[Server thread/INFO]: [MCCoroutineSamplePlugin/onDisableAsync] Is starting on Thread:Server thread/55/primaryThread=true
[Server thread/INFO]: [MCCoroutineSamplePlugin/onDisableAsync] Number 1:Server thread/55/primaryThread=true
[Server thread/INFO]: [MCCoroutineSamplePlugin/onDisableAsync] Is ending on Thread:Server thread/55/primaryThread=true
```

However, the message ``[MCCoroutineSamplePlugin/onDisableAsync] Number 2`` will not printed,
because ``plugin.mcCoroutineConfiguration.disposePluginSession()`` is called first (context switch of delay).

This means, we need to use ``runBlocking`` anyway:

```kotlin
override fun onDisable() {
    val plugin = this
    println("[MCCoroutineSamplePlugin/onDisableAsync] Is starting on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")

    runBlocking {
        plugin.launch {
            println("[MCCoroutineSamplePlugin/onDisableAsync] Number 1:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")
            delay(500)
            println("[MCCoroutineSamplePlugin/onDisableAsync] Number 2:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")
        }.join()
    }

    plugin.mcCoroutineConfiguration.disposePluginSession()
    println("[MCCoroutineSamplePlugin/onDisableAsync] Is ending on Thread:${Thread.currentThread().name}/${Thread.currentThread().id}/primaryThread=${Bukkit.isPrimaryThread()}")
}
```

```
[Server thread/INFO]: [MCCoroutine-Sample] Disabling MCCoroutine-Sample
[Server thread/INFO]: [MCCoroutineSamplePlugin/onDisableAsync] Is starting on Thread:Server thread/55/primaryThread=true
[Server thread/INFO]: [MCCoroutineSamplePlugin/onDisableAsync] Number 1:Server thread/55/primaryThread=true
[kotlinx.coroutines.DefaultExecutor/INFO]: [MCCoroutineSamplePlugin/onDisableAsync] Number 2:kotlinx.coroutines.DefaultExecutor/133/primaryThread=false
[Server thread/INFO]: [MCCoroutineSamplePlugin/onDisableAsync] Is ending on Thread:Server thread/55/primaryThread=true
```

This helps, however it is important to notice that the thread
executing ``MCCoroutineSamplePlugin/onDisableAsync] Number 2`` is **no longer the primary thread** even though
we are using the ``plugin.launch`` scope, which should guarantee this.
After the ``BukkitScheduler`` has been shutdown, MCCoroutine is no longer able to guarantee any context switches.
Depending on your use case, you may or may not care about that.

**Therefore, think twice if you really want to have so much control. You are on your own, if you set the
shutdownStrategy to manual.**

### Waiting for jobs to complete

One useful case, where you want to set the shutdownStrategy to manual is to be able to wait for long running jobs to
complete before you disable the plugin.

```kotlin
private var longRunningJob: Job? = null

override fun onEnable() {
    val plugin = this
    plugin.mcCoroutineConfiguration.shutdownStrategy = ShutdownStrategy.MANUAL

    longRunningJob = plugin.launch {
        delay(10000)
        println("Over")
    }
}

override fun onDisable() {
    runBlocking {
        longRunningJob!!.join()
    }

    val plugin = this
    plugin.mcCoroutineConfiguration.disposePluginSession()
}
```

```
[Server thread/INFO]: [MCCoroutine-Sample] Disabling MCCoroutine-Sample
[kotlinx.coroutines.DefaultExecutor/INFO]: Over
```

### Waiting for all jobs to complete

You can also wait for all of your spawned open jobs to complete.

```kotlin
override fun onEnable() {
    val plugin = this
    plugin.mcCoroutineConfiguration.shutdownStrategy = ShutdownStrategy.MANUAL

    plugin.launch {
        delay(10000)
        println("Over")
    }
}

override fun onDisable() {
    val plugin = this

    runBlocking {
        plugin.scope.coroutineContext[Job]!!.children.forEach { childJob ->
            childJob.join()
        }
    }

    plugin.mcCoroutineConfiguration.disposePluginSession()
}
```

```
[Server thread/INFO]: [MCCoroutine-Sample] Disabling MCCoroutine-Sample
[kotlinx.coroutines.DefaultExecutor/INFO]: Over
```




