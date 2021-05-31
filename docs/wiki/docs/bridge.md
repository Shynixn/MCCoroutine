# Bridging non-suspendable functions to suspendable functions

This guide continues the guide 'Creating a new Plugin' and describes how to bridge non suspendable code with suspendable implementations.

### 1. Adding plugin launch

Use the extension method ``plugin.launch{}`` to enter a suspendable context on the bukkit primary thread.

````kotlin
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

### 2. Understanding the execution order.

By allowing to append ``suspend`` to your listeners and command executors it is highly unlikely that you need to use this function. 
Still, it is important to understand the execution order in this case.

````kotlin
class Foo(private val plugin : Plugin) {

    fun bar() {
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
"I am first
"I am second
"I am third"
"I am fifth"
````

### 3. Do not use runBlocking

Using ``runBlocking`` in production code is very bad as it annihilates any improvements, we have made by using coroutines. 
MCCoroutine manipulates the Bukkit Scheduler to rescue the Bukkit Primary Thread Context if it is being used but ``plugin.launch{}``
is almost always the function you want to use instead.

### 4. Test the Foo class

Connect to the foo class in any way and call it for testing.
