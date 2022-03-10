@file:Suppress("UNUSED_PARAMETER")

package integrationtest

import com.github.shynixn.mccoroutine.EventExecutionType
import com.github.shynixn.mccoroutine.callSuspendingEvent
import com.github.shynixn.mccoroutine.launch
import com.github.shynixn.mccoroutine.registerSuspendingEvents
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import org.bukkit.Server
import org.bukkit.command.SimpleCommandMap
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.SimplePluginManager
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.concurrent.Executors
import java.util.logging.Logger
import kotlin.test.assertEquals

class EventPriorityTest {
    /**
     * Given
     *  a call of a suspending event
     *  When
     *  callSuspendingEvent is called with concurrent event execution
     *  Then
     *  events should be called in the correct order but executed concurrently.
     */
    @Test
    fun callSuspendingEvent_ConcurrentEventReceivers_ShouldCallEventsInOrder() {
        // Arrange
        val plugin = Mockito.mock(org.bukkit.plugin.Plugin::class.java)
        Mockito.`when`(plugin.isEnabled).thenReturn(true)
        val server = Mockito.mock(Server::class.java)
        val scheduler = Mockito.mock(BukkitScheduler::class.java)
        Mockito.`when`(plugin.server).thenReturn(server)
        Mockito.`when`(server.scheduler).thenReturn(scheduler)
        val executionService = Executors.newFixedThreadPool(1)
        var minecraftThreadId = 0L
        executionService.submit {
            minecraftThreadId = Thread.currentThread().id
        }
        Mockito.`when`(scheduler.runTask(Mockito.any(Plugin::class.java), Mockito.any(Runnable::class.java)))
            .thenAnswer {
                executionService.submit(it.getArgument(1))
                Mockito.mock(BukkitTask::class.java)
            }
        Mockito.`when`(server.pluginManager).thenReturn(Mockito.mock(PluginManager::class.java))
        plugin.launch { }
        val pluginManager = SimplePluginManager(server, Mockito.mock(SimpleCommandMap::class.java))
        Mockito.`when`(server.pluginManager).thenReturn(pluginManager)
        Mockito.`when`(server.logger).thenReturn(Logger.getGlobal())
        Mockito.`when`(plugin.description).thenReturn(Mockito.mock(PluginDescriptionFile::class.java))
        val player = Mockito.mock(Player::class.java)
        val playerJoinEvent = PlayerJoinEvent(player, null)
        val classUnderTest = TestEventListener()
        plugin.server.pluginManager.registerSuspendingEvents(classUnderTest, plugin)

        // Act
        runBlocking {
            try {
                plugin.server.pluginManager.callSuspendingEvent(playerJoinEvent, plugin).joinAll()
            } catch (e: Exception) {
                e.toString()
            }
        }
        val actualResult = classUnderTest.resultList

        // Assert
        assertEquals(minecraftThreadId, classUnderTest.startThreadId)
        assertEquals(minecraftThreadId, classUnderTest.endThreadId)
        assertEquals(2, actualResult[0])
        assertEquals(3, actualResult[1])
        assertEquals(1, actualResult[2])
    }

    /**
     * Given
     *  a call of a suspending event
     *  When
     *  callSuspendingEvent is called with consecutive event execution
     *  Then
     *  events should be called in the correct order but executed consecutive.
     */
    @Test
    fun callSuspendingEvent_ConsecutiveEventReceivers_ShouldCallEventsInOrder() {
        // Arrange
        val plugin = Mockito.mock(org.bukkit.plugin.Plugin::class.java)
        Mockito.`when`(plugin.isEnabled).thenReturn(true)
        val server = Mockito.mock(Server::class.java)
        val scheduler = Mockito.mock(BukkitScheduler::class.java)
        Mockito.`when`(plugin.server).thenReturn(server)
        Mockito.`when`(server.scheduler).thenReturn(scheduler)
        val executionService = Executors.newFixedThreadPool(1)
        var minecraftThreadId = 0L
        executionService.submit {
            minecraftThreadId = Thread.currentThread().id
        }
        Mockito.`when`(scheduler.runTask(Mockito.any(Plugin::class.java), Mockito.any(Runnable::class.java)))
            .thenAnswer {
                executionService.submit(it.getArgument(1))
                Mockito.mock(BukkitTask::class.java)
            }
        Mockito.`when`(server.pluginManager).thenReturn(Mockito.mock(PluginManager::class.java))
        plugin.launch { }
        val pluginManager = SimplePluginManager(server, Mockito.mock(SimpleCommandMap::class.java))
        Mockito.`when`(server.pluginManager).thenReturn(pluginManager)
        Mockito.`when`(server.logger).thenReturn(Logger.getGlobal())
        Mockito.`when`(plugin.description).thenReturn(Mockito.mock(PluginDescriptionFile::class.java))
        val player = Mockito.mock(Player::class.java)
        val playerJoinEvent = PlayerJoinEvent(player, null)
        val classUnderTest = TestEventListener()
        plugin.server.pluginManager.registerSuspendingEvents(classUnderTest, plugin)

        // Act
        runBlocking {
            try {
                plugin.server.pluginManager.callSuspendingEvent(playerJoinEvent, plugin, EventExecutionType.Consecutive).joinAll()
            } catch (e: Exception) {
                e.toString()
            }
        }
        val actualResult = classUnderTest.resultList

        // Assert
        assertEquals(minecraftThreadId, classUnderTest.startThreadId)
        assertEquals(minecraftThreadId, classUnderTest.endThreadId)
        assertEquals(1, actualResult[0])
        assertEquals(2, actualResult[1])
        assertEquals(3, actualResult[2])
    }

    private class TestEventListener : Listener {
        val resultList = ArrayList<Int>()
        var startThreadId = 0L
        var endThreadId = 0L

        @EventHandler(priority = EventPriority.LOW)
        suspend fun onPlayerJoinEventLow(event: PlayerJoinEvent) {
            startThreadId = Thread.currentThread().id
            delay(200)
            resultList.add(1)
            endThreadId = Thread.currentThread().id
        }

        @EventHandler(priority = EventPriority.NORMAL)
        fun onPlayerJoinEventNormal(event: PlayerJoinEvent) {
            resultList.add(2)
        }

        @EventHandler(priority = EventPriority.HIGH)
        suspend fun onPlayerJoinEventHigh(event: PlayerJoinEvent) {
            delay(100)
            resultList.add(3)
        }
    }
}
