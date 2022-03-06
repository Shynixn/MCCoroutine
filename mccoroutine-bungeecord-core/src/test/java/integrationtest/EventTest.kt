package integrationtest

import com.github.shynixn.mccoroutine.asyncDispatcher
import com.github.shynixn.mccoroutine.registerSuspendingListener
import kotlinx.coroutines.withContext
import net.md_5.bungee.api.ProxyConfig
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.PluginDescription
import net.md_5.bungee.api.plugin.PluginManager
import net.md_5.bungee.api.scheduler.ScheduledTask
import net.md_5.bungee.api.scheduler.TaskScheduler
import net.md_5.bungee.event.EventHandler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.concurrent.Executors
import java.util.logging.Logger

class EventTest {
    /**
     * Given
     *  a suspending Listener with suspending operations
     * When
     *  a new event is called
     * Then
     *  the operations should be performed on the correct threads.
     */
    @Test
    fun dispatchEvent_SuspendingListener_ShouldDispatchOnDifferentThreads() {
        // Arrange
        val plugin = Mockito.mock(Plugin::class.java)
        val proxyServer = Mockito.mock(ProxyServer::class.java)
        Mockito.`when`(proxyServer.logger).thenReturn(Logger.getAnonymousLogger())
        val pluginManager = PluginManager(proxyServer)
        val player = Mockito.mock(ProxiedPlayer::class.java)
        val executorService = Executors.newFixedThreadPool(4)
        Mockito.`when`(plugin.proxy).thenReturn(proxyServer)
        Mockito.`when`(plugin.executorService).thenReturn(executorService)
        Mockito.`when`(proxyServer.pluginManager).thenReturn(pluginManager)
        Mockito.`when`(plugin.description).thenReturn(PluginDescription())
        Mockito.`when`(plugin.logger).thenReturn(Logger.getAnonymousLogger())
        Mockito.`when`(proxyServer.config).thenReturn(Mockito.mock(ProxyConfig::class.java))
        val taskScheduler = Mockito.mock(TaskScheduler::class.java)
        Mockito.`when`(proxyServer.scheduler).thenReturn(taskScheduler)
        Mockito.`when`(taskScheduler.runAsync(Mockito.any<Plugin>(), Mockito.any(Runnable::class.java))).thenAnswer {
            executorService.submit(it.arguments[1] as Runnable)
            Mockito.mock(ScheduledTask::class.java)
        }

        // Act
        val listenerUnderTest = SampleEventListener(plugin)
        pluginManager.registerSuspendingListener(plugin, listenerUnderTest)
        pluginManager.callEvent(PostLoginEvent(player))
        Thread.sleep(50)
        val testThreadId = Thread.currentThread().id

        // Assert
        Assertions.assertEquals(testThreadId, listenerUnderTest.calledThread)
        Assertions.assertNotEquals(testThreadId, listenerUnderTest.ioThread)
        Assertions.assertNotEquals(testThreadId, listenerUnderTest.leaveThread)
        Assertions.assertNotEquals(0, listenerUnderTest.calledThread)
        Assertions.assertNotEquals(0, listenerUnderTest.ioThread)
        Assertions.assertNotEquals(0, listenerUnderTest.leaveThread)
        Assertions.assertNotEquals(listenerUnderTest.calledThread, listenerUnderTest.ioThread)
        Assertions.assertNotEquals(listenerUnderTest.leaveThread, listenerUnderTest.ioThread)
        Assertions.assertNotEquals(listenerUnderTest.calledThread, listenerUnderTest.leaveThread)
        Assertions.assertTrue(listenerUnderTest.event2Called)
    }

    class SampleEventListener(private val plugin: Plugin) : Listener {
        var calledThread: Long = 0
        var ioThread: Long = 0
        var leaveThread: Long = 0
        var event2Called = false

        @EventHandler
        suspend fun onPostLoginEvent(event: PostLoginEvent) {
            calledThread = Thread.currentThread().id

            withContext(plugin.asyncDispatcher) {
                ioThread = Thread.currentThread().id
            }

            leaveThread = Thread.currentThread().id
        }

        @EventHandler
        fun onPostLoginEvent2(event: PostLoginEvent) {
            event2Called = true
        }
    }
}
