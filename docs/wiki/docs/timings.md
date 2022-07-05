# Timing Measurements

(This site is only relevant for Spigot, Paper and CraftBukkit)

It is often the case, that we want to measure performance using timings https://timings.spigotmc.org/.
However, Coroutines do not yield a meaningful task name per default e.g. `` Task: CancellableContinuationImpl(Single)``, which makes it hard to debug for performance
problems.

As a solution, it is possible to pass an instance of ``CoroutineTimings``, which is used to give the coroutine
and its main thread tasks one meaningful name.

For example, if you are starting a new coroutine like this:

````kotlin
plugin.launch {
    println("Please say hello in 2 seconds")
    delay(2000) // Delay for 2000 milliseconds
    println("hello")
}
````

Change it to the following:

````kotlin
plugin.launch(plugin.minecraftDispatcher + object : CoroutineTimings() {}) {
    println("Please say hello in 2 seconds")
    delay(2000) // Delay for 2000 milliseconds
    println("hello")
}
````

## Command Executors

You can also assign a name to a ``SuspendingCommandExecutor``. For this, add an object called ``coroutineTimings`` to your class implementing ``SuspendingCommandExecutor``.

````kotlin
class MyCommandExecutor : SuspendingCommandExecutor {
    // Reference used for naming.
    companion object coroutineTimings : CoroutineTimings()

    override suspend fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        TODO("Not yet implemented")
    }
}
````

Register the ``SuspendingCommandExecutor`` in your plugin class as follows:

````kotlin
val myCommandExecutor = MyCommandExecutor()
this.getCommand("mycommand")!!.setSuspendingExecutor(minecraftDispatcher + MyCommandExecutor.coroutineTimings, myCommandExecutor)
````

## Events

Event measurements are currently not supported by MCCoroutine. 

You can temporarily remove ``suspend``
from your event method, use ``plugin.launch(plugin.minecraftDispatcher + object : CoroutineTimings() {}) {}``, 
measure the time and then readd ``suspend again.``
