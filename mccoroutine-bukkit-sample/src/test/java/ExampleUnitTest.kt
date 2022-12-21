import com.github.shynixn.mccoroutine.bukkit.MCCoroutine
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.sample.impl.FakeDatabase
import com.github.shynixn.mccoroutine.bukkit.sample.impl.UserDataCache
import com.github.shynixn.mccoroutine.bukkit.test.TestMCCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import kotlin.test.assertEquals

class ExampleUnitTest {

    init {
        /**
         * This switches MCCoroutine to the test implementation of MCCoroutine.
         * It affects all the tests in the current session.
         */
        MCCoroutine.Driver = TestMCCoroutine.Driver
    }

    @Test
    fun test1() {
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

    @Test
    fun test2() {
        // Uses the mocking library called Mockito to mock a plugin and a player instance.
        // It does not matter how you create a plugin instance. Other mocking libraries work as well.
        val plugin = Mockito.mock(Plugin::class.java)
        val player = Mockito.mock(Player::class.java)

        // The 'Unit' we want to test.
        val fakeDatabase = FakeDatabase()
        val classUnderTest = UserDataCache(plugin, fakeDatabase)

        // Act and Assert.
        runBlocking(plugin.minecraftDispatcher) {
            val data1 = classUnderTest.getUserDataFromPlayerAsync(player)
            val data2 = classUnderTest.getUserDataFromPlayerAsync(player)

            // Should be the same instance because of cache hit. Hashcode should be equal.
            assertEquals(data1, data2)
        }
    }
}
