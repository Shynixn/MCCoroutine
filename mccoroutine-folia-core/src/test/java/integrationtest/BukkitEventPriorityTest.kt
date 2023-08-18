@file:Suppress("UNUSED_PARAMETER")

package integrationtest

import com.github.shynixn.mccoroutine.folia.callSuspendingEvent
import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.github.shynixn.mccoroutine.folia.registerSuspendingEvents
import helper.MockedBukkitServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

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
        val playerJoinEvent = PlayerJoinEvent(player, null as String?)
        val classUnderTest = TestEventListener()
        plugin.server.pluginManager.registerSuspendingEvents(classUnderTest, plugin)

        // Act
        runBlocking(plugin.globalRegionDispatcher) {
            plugin.server.pluginManager.callSuspendingEvent(playerJoinEvent, plugin).joinAll()
        }
        val actualResult = classUnderTest.resultList

        // Assert
        Assertions.assertEquals(server.mainThreadId, classUnderTest.startThreadId)
        Assertions.assertEquals(server.mainThreadId, classUnderTest.endThreadId)
        Assertions.assertEquals(2, actualResult[0])
        Assertions.assertEquals(3, actualResult[1])
        Assertions.assertEquals(1, actualResult[2])
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
        val playerJoinEvent = PlayerJoinEvent(player, null as String?)
        val classUnderTest = TestEventListener()
        plugin.server.pluginManager.registerSuspendingEvents(classUnderTest, plugin)

        // Act
        runBlocking(plugin.globalRegionDispatcher) {
            plugin.server.pluginManager.callSuspendingEvent(playerJoinEvent, plugin, com.github.shynixn.mccoroutine.folia.EventExecutionType.Consecutive)
                .joinAll()
        }
        val actualResult = classUnderTest.resultList

        // Assert
        Assertions.assertEquals(server.mainThreadId, classUnderTest.startThreadId)
        Assertions.assertEquals(server.mainThreadId, classUnderTest.endThreadId)
        Assertions.assertEquals(1, actualResult[0])
        Assertions.assertEquals(2, actualResult[1])
        Assertions.assertEquals(3, actualResult[2])
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
