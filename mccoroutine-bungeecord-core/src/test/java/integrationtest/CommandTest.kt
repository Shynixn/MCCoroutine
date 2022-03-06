package integrationtest

import com.github.shynixn.mccoroutine.SuspendingCommand
import com.github.shynixn.mccoroutine.asyncDispatcher
import com.github.shynixn.mccoroutine.registerSuspendingCommand
import kotlinx.coroutines.withContext
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyConfig
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.PluginDescription
import net.md_5.bungee.api.plugin.PluginManager
import net.md_5.bungee.api.scheduler.ScheduledTask
import net.md_5.bungee.api.scheduler.TaskScheduler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.concurrent.Executors
import java.util.logging.Logger

class CommandTest {
    /**
     * Given
     *  a suspending CommandExecutor with suspending operations
     * When
     *  a new command is dispatched
     * Then
     *  the operations should be performed on the correct threads.
     */
    @Test
    fun dispatchCommand_SuspendingCommandExecutor_ShouldDispatchOnDifferentThreads() {
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
        Mockito.`when`(proxyServer.config).thenReturn(Mockito.mock(ProxyConfig::class.java))
        val taskScheduler = Mockito.mock(TaskScheduler::class.java)
        Mockito.`when`(proxyServer.scheduler).thenReturn(taskScheduler)
        Mockito.`when`(taskScheduler.runAsync(Mockito.any<Plugin>(), Mockito.any(Runnable::class.java))).thenAnswer {
            executorService.submit(it.arguments[1] as Runnable)
            Mockito.mock(ScheduledTask::class.java)
        }

        // Act
        val commandUnderTest = SampleSuspendingCommandExecutor(plugin)
        pluginManager.registerSuspendingCommand(plugin, commandUnderTest)
        pluginManager.dispatchCommand(player, "IntegrationTest do something")
        Thread.sleep(2000)
        val testThreadId = Thread.currentThread().id

        // Assert
        Assertions.assertEquals(testThreadId, commandUnderTest.calledThread)
        Assertions.assertNotEquals(testThreadId, commandUnderTest.ioThread)
        Assertions.assertNotEquals(testThreadId, commandUnderTest.leaveThread)
        Assertions.assertNotEquals(commandUnderTest.calledThread, commandUnderTest.ioThread)
        Assertions.assertNotEquals(commandUnderTest.leaveThread, commandUnderTest.ioThread)
        Assertions.assertNotEquals(commandUnderTest.calledThread, commandUnderTest.leaveThread)
    }

    private class SampleSuspendingCommandExecutor(private val plugin: Plugin) : SuspendingCommand("IntegrationTest") {
        var calledThread: Long = 0
        var ioThread: Long = 0
        var leaveThread: Long = 0

        override suspend fun execute(sender: CommandSender, args: Array<out String>) {
            calledThread = Thread.currentThread().id

            withContext(plugin.asyncDispatcher) {
                ioThread = Thread.currentThread().id
                Thread.sleep(500)
            }

            leaveThread = Thread.currentThread().id
        }
    }
}
