@file:Suppress("UNUSED_PARAMETER")

package integrationtest

import com.github.shynixn.mccoroutine.bukkit.callSuspendingEvent
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import helper.MockedBukkitServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import kotlin.test.assertEquals

class BukkitEventPriorityTest {
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
        val server = MockedBukkitServer()
        val plugin = server.boot()
        val player = Mockito.mock(Player::class.java)
        val playerJoinEvent = PlayerJoinEvent(player, null)
        val classUnderTest = TestEventListener()
        plugin.server.pluginManager.registerSuspendingEvents(classUnderTest, plugin)

        // Act
        runBlocking(plugin.minecraftDispatcher) {
            plugin.server.pluginManager.callSuspendingEvent(playerJoinEvent, plugin).joinAll()
        }
        val actualResult = classUnderTest.resultList

        // Assert
        assertEquals(server.mainThreadId, classUnderTest.startThreadId)
        assertEquals(server.mainThreadId, classUnderTest.endThreadId)
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
        val server = MockedBukkitServer()
        val plugin = server.boot()
        val player = Mockito.mock(Player::class.java)
        val playerJoinEvent = PlayerJoinEvent(player, null)
        val classUnderTest = TestEventListener()
        plugin.server.pluginManager.registerSuspendingEvents(classUnderTest, plugin)

        // Act
        runBlocking(plugin.minecraftDispatcher) {
            plugin.server.pluginManager.callSuspendingEvent(playerJoinEvent, plugin, com.github.shynixn.mccoroutine.bukkit.EventExecutionType.Consecutive)
                .joinAll()
        }
        val actualResult = classUnderTest.resultList

        // Assert
        assertEquals(server.mainThreadId, classUnderTest.startThreadId)
        assertEquals(server.mainThreadId, classUnderTest.endThreadId)
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
