# Exception Handling

MCCoroutine implements exception handling as explained by the official [Coroutine docs](https://kotlinlang.org/docs/exception-handling.html).

If an exception is not caught (e.g. an exception is thrown in a suspendable commandexecutor or listener), the exception is propagated upwards to MCCoroutine.

## Default Exception Behaviour

By default, MCCoroutine logs every exception except ``CoroutineCancellation``, which is thrown when a job is cancelled.

````kotlin
logger.log(
    Level.SEVERE,
    "This is not an error of MCCoroutine! See sub exception for details.",
    exception
)
````

## Custom Exception Behaviour

You can handle exceptions by yourself by listening to the ``MCCoroutineExceptionEvent``. This event is sent to the event bus of the minecraft frame work (e.g. Bukkit, Sponge, BungeeCord) 
and can be used for logging. The following points should be considered:

* The event arrives at the main thread (Bukkit, Sponge, Minestom)
* The event is also called for ``CoroutineCancellation``
* Exceptions arrive for every plugin using MCCoroutine. Check if ``event.plugin`` equals your plugin.
* You can cancel the event to disable logging the event with the default exception behaviour
* You can make this event a ``suspend`` function, however put a ``try-catch`` over the entire function. Otherwise, any
  exception which occur while logging the original exception could stack indefinitely which eventually causes a ``OutOfMemoryException``



