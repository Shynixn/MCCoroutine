package unittest

import com.github.shynixn.mccoroutine.dispatcher.AsyncCoroutineDispatcher
import helper.any
import org.bukkit.Server
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AsyncCoroutineDispatcherTest {
    /**
     * Given a session on the primary thread
     * When dispatch is called
     * then the async scheduler should be called.
     */
    @Test
    fun dispatch_OnPrimaryThread_ShouldCallAsyncScheduler() {
        // Arrange
        val plugin = Mockito.mock(Plugin::class.java)
        val server = Mockito.mock(Server::class.java)
        val scheduler = Mockito.mock(BukkitScheduler::class.java)
        var schedulerRun = false
        Mockito.`when`(scheduler.runTaskAsynchronously(any(Plugin::class.java), any(Runnable::class.java))).then {
            schedulerRun = true
            Mockito.mock(BukkitTask::class.java)
        }
        Mockito.`when`(server.scheduler).thenReturn(scheduler)
        Mockito.`when`(plugin.server).thenReturn(server)
        Mockito.`when`(server.isPrimaryThread).thenReturn(true)
        var blockRun = false
        val runnable = Runnable {
            blockRun = true
        }
        val classUnderTest = createWithDependencies(plugin)

        // Act
        classUnderTest.dispatch(Mockito.mock(CoroutineContext::class.java), runnable)

        // Assert
        assertFalse(blockRun)
        assertTrue(schedulerRun)
    }

    /**
     * Given a session not on the primary thread
     * When dispatch is called
     * then the block should be called.
     */
    @Test
    fun dispatch_NotOnPrimaryThread_ShouldCallBlock() {
        // Arrange
        val plugin = Mockito.mock(Plugin::class.java)
        val server = Mockito.mock(Server::class.java)
        val scheduler = Mockito.mock(BukkitScheduler::class.java)
        var schedulerRun = false
        Mockito.`when`(scheduler.runTaskAsynchronously(any(Plugin::class.java), any(Runnable::class.java))).then {
            schedulerRun = true
            Mockito.mock(BukkitTask::class.java)
        }
        Mockito.`when`(server.scheduler).thenReturn(scheduler)
        Mockito.`when`(plugin.server).thenReturn(server)
        Mockito.`when`(server.isPrimaryThread).thenReturn(false)
        var blockRun = false
        val runnable = Runnable {
            blockRun = true
        }
        val classUnderTest = createWithDependencies(plugin)

        // Act
        classUnderTest.dispatch(Mockito.mock(CoroutineContext::class.java), runnable)

        // Assert
        assertTrue(blockRun)
        assertFalse(schedulerRun)
    }

    private fun createWithDependencies(plugin: Plugin): AsyncCoroutineDispatcher {
        return AsyncCoroutineDispatcher(plugin)
    }
}
