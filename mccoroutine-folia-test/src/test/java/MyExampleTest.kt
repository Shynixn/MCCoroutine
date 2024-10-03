import com.github.shynixn.mccoroutine.folia.*
import com.github.shynixn.mccoroutine.folia.test.TestMCCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito

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
        runBlocking(plugin.mainDispatcher) {
            println("Step 1: " + Thread.currentThread().name + "/" + Thread.currentThread().id)

            withContext(Dispatchers.IO) {
                println("Step 2: " + Thread.currentThread().name + "/" + Thread.currentThread().id)
                delay(1000)
            }

            println("Step 3: " + Thread.currentThread().name + "/" + Thread.currentThread().id)

            withContext(plugin.entityDispatcher(Mockito.mock(Entity::class.java))) {
                println("Step 4: " + Thread.currentThread().name + "/" + Thread.currentThread().id)
                delay(1000)
            }

            withContext(plugin.regionDispatcher(Location(Mockito.mock(World::class.java), 3.0, 3.0, 3.0))) {
                println("Step 5: " + Thread.currentThread().name + "/" + Thread.currentThread().id)
                delay(1000)
            }

            println("Step 6: " + Thread.currentThread().name + "/" + Thread.currentThread().id)

            // Just as an example, we can also use plugin.launch
            plugin.launch {
                println("Step 7: " + Thread.currentThread().name + "/" + Thread.currentThread().id)
                delay(1000)
                println("Step 8: " + Thread.currentThread().name + "/" + Thread.currentThread().id)
            }.join() // Wait until finished.
        }
    }
}
