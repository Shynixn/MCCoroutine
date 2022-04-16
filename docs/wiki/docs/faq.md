# FAQ

This page explains the most common questions regarding MCCoroutine.

### How is MCCoroutine implemented?

MCCoroutine simply wraps the existing schedulers of the minecraft frameworks. For example, when you suspend a function using
``withContext``, MCCoroutine sends new tasks to the Bukkit scheduler if necessary. Every consideration about Bukkit schedulers applies to MCCoroutine as well.

### Does MCCoroutine need more RAM?

MCCoroutine does not create any resources like threads or threadPools. This means MCCoroutine does not have any overhead. However, Kotlin Coroutines 
contains additional thread pools which may increase memory usage slightly. Take a look the the official Kotlin Coroutine docs for details.

### Are Suspendable Listeners/Command Executors slower?

No, they are as fast as ordinary listeners and command executors. The registration of them is slightly slower
because reflection calls are used to create them. Once players join the server and events arrive, they are the same speed.

### How to cancel suspendable events?

The following example is not possible. You cannot cancel events after you have suspended the context for the
very first time. The event has already happened, and the outcome has already been decided.

````kotlin
@EventHandler
suspend fun onPlayerInteractEvent(event: PlayerInteractEvent) {
    withContext(Dispatchers.IO){
        // e.g. read file/database
        delay(50)
    }
    // Cancellation is not possible at this point.
    event.isCancelled = true;
}
````

Cancelling events before the first suspension is still possible.

````kotlin
@EventHandler
suspend fun onPlayerInteractEvent(event: PlayerInteractEvent) {
    // Cancellation is possible at this point.
    event.isCancelled = true;
    
    withContext(Dispatchers.IO){
        // e.g. read file/database
        delay(50)
    }
}
````

