# Suspending API Functions

This page explains how you can switch into a suspendable scope anywhere in your plugin.

## Plugin launch

Use the extension method ``plugin.launch{}`` to enter a suspendable context on any thread. ``plugin.launch{}`` is threadsafe
and can be called from any thread at any time. 

This function also accepts two optional parameters ``context: CoroutineContext`` and ``start: CoroutineStart``. 
It is not recommend changing them because it is very likely to make mistakes here. It requires a very deep understanding of Java threads, Java thread pools, Kotlin Coroutines and how each
minecraft framework uses schedulers to dispatch tasks correctly when using other parameters. The default parameters are almost always correct.

````kotlin
import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.delay
import org.bukkit.plugin.Plugin

class Foo(private val plugin : Plugin) {

    fun bar() {
        val job = plugin.launch {
            delay(1000)
            bob()
        }
    }
    
    private suspend fun bob(){
    }
}
````

## Plugin launch execution order

It is recommended to use suspendable command executors or suspendable listeners to switch to ``suspend`` functions. However, if you
use ``plugin.launch``, it is important to understand the execution order.

=== "Bukkit and Sponge"

    ````kotlin
    class Foo(private val plugin : Plugin) {
    
        fun bar() {
            // Main Thread
            println("I am first")
            
            val job = plugin.launch {
                println("I am second") // The context is not suspended when switched to the same suspendable context.
                delay(1000)
                println("I am fourth") // The context is given back after 1000 milliseconds and continuous here.
                bob()
            }
            
            // When calling delay the suspendable context is suspended and the original context immediately continuous here.
            println("I am third")
        }
    
        private suspend fun bob(){
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

=== "BungeeCord and Velocity"

    As BungeeCord and Velocity do not have a main thread, the execution order is slightly different.
    
    ````kotlin
    class Foo(private val plugin : Plugin) {
    
        fun bar() {
            // Any thread
            println("I am first")
            
            val job = plugin.launch {
                println("I am second") // The context is suspended because it is not known which context was present before.
                delay(1000)
                println("I am fourth") // The context is given back after 1000 milliseconds and continuous here.
                bob()
            }
            
            // When calling delay the suspendable context is suspended and the original context immediately continuous here.
            println("I am third")
        }
    
        private suspend fun bob(){
            println("I am fifth")
        }
    }
    ````
    
    ````kotlin
    "I am first"
    "I am third"
    "I am second"
    "I am fourth"
    "I am fifth"
    ````

    You can actually change this behaviour by passing a different parameter to ``start``. This might be useful for 
    other libraries wanting to use MCCoroutine.

    ````kotlin
    class Foo(private val plugin : Plugin) {
    
        fun bar() {
            // Any thread
            println("I am first")
            
            val job = plugin.launch(start = CoroutineStart.UNDISPATCHED) {
                println("I am second") // The context is not suspended because start is undispatched.
                delay(1000)
                println("I am fourth") // The context is given back after 1000 milliseconds and continuous here.
                bob()
            }
            
            // When calling delay the suspendable context is suspended and the original context immediately continuous here.
            println("I am third")
        }
    
        private suspend fun bob(){
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

## Do not use runBlocking

Using ``runBlocking`` in production code is very bad as it annihilates any improvements, we have made by using coroutines. 
MCCoroutine manipulates the Bukkit Scheduler to allow ``runBlocking`` during startup and disable, but ``plugin.launch{}``
is almost always the function you want to use instead.
