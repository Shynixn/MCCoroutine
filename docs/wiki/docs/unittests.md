# Unit-Tests with MCCoroutine

(This site is only relevant for Spigot, Paper and CraftBukkit. If you need Unit-Tests support for BungeeCord, Sponge or
Velocity, please submit an issue on GitHub)

If you try to write Unit- or IntegrationTests for your Minecraft plugin, you may need to test suspend functions. These
functions
may use ``plugin.launch{...}`` or other extension methods from MCCoroutine.

However, extensive mocking is required to get MCCoroutine to work without a running server. As a solution to this
problem, a new test dependency is available, which
closely simulates MCCoroutine under real conditions. This means you can focus on writing your tests and get a very close
feedback to the real environment.

### 1. Add the dependency

**Do not** shade this library into your final plugin.jar file. This should only be available during UnitTests.

```kotlin
dependencies {
    testImplementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-test:2.19.0")
}
```

### 2. Create a test method

```kotlin
import org.junit.jupiter.api.Test

class MyExampleTest {
    @Test
    fun testCase01(){
    }
}
```

### 3. Change the MCCoroutine Production-Driver to the Test-Driver


```kotlin
import org.junit.jupiter.api.Test

class MyExampleTest {
    
    init {
        /**
         * This switches MCCoroutine to the test implementation of MCCoroutine.
         * It affects all the tests in the current session.
         */
        MCCoroutine.Driver = TestMCCoroutine.Driver
    }
    
    @Test
    fun testCase01(){
    }
}
```

#### 4. Use MCCoroutine in the same way as on your server

```kotlin
import org.junit.jupiter.api.Test

class MyExampleTest {
    
    init {
        /**
         * This switches MCCoroutine to the test implementation of MCCoroutine.
         * It affects all the tests in the current session.
         */
        MCCoroutine.Driver = TestMCCoroutine.Driver
    }
    
    @Test
    fun testCase01(){
        // Uses the mocking library called Mockito to mock a plugin instance.
        // It does not matter how you create a plugin instance. Other mocking libraries work as well.
        val plugin = Mockito.mock(Plugin::class.java)

        // We need to use runBlocking here, otherwise the test exits early
        runBlocking(plugin.minecraftDispatcher) {
            println("Step 1: " + Thread.currentThread().name + "/" + Thread.currentThread().id)

            withContext(Dispatchers.IO) {
                println("Step 2: " + Thread.currentThread().name + "/" + Thread.currentThread().id)
                delay(1000)
            }

            println("Step 3: " + Thread.currentThread().name + "/" + Thread.currentThread().id)

            // As always, prefer using Dispatchers.IO instead of plugin.asyncDispatcher.
            withContext(plugin.asyncDispatcher) {
                println("Step 4: " + Thread.currentThread().name + "/" + Thread.currentThread().id)
                delay(1000)
            }

            println("Step 5: " + Thread.currentThread().name + "/" + Thread.currentThread().id)

            // Just as an example, we can also use plugin.launch
            plugin.launch {
                println("Step 6: " + Thread.currentThread().name + "/" + Thread.currentThread().id)
                delay(1000)
                println("Step 7: " + Thread.currentThread().name + "/" + Thread.currentThread().id)
            }.join() // Wait until finished.
        }
    }
}
```

    







