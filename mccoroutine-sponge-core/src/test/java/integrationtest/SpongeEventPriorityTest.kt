@file:Suppress("UNUSED_PARAMETER")

package integrationtest

import com.github.shynixn.mccoroutine.sponge.EventExecutionType
import com.github.shynixn.mccoroutine.sponge.postSuspending
import com.github.shynixn.mccoroutine.sponge.registerSuspendingListeners
import helper.MockedSpongeServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.spongepowered.api.Sponge
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.Order
import org.spongepowered.api.event.network.ClientConnectionEvent
import kotlin.test.assertEquals

class SpongeEventPriorityTest {
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
        val mockedSpongeServer = MockedSpongeServer()
        val plugin = mockedSpongeServer.boot()
        val classUnderTest = TestEventListener()
        val clientConnectionEvent = Mockito.mock(ClientConnectionEvent.Join::class.java)
        Sponge.getEventManager().registerSuspendingListeners(plugin, classUnderTest)

        // Act
        runBlocking {
            try {
                Sponge.getEventManager().postSuspending(clientConnectionEvent, plugin).joinAll()
            } catch (e: Exception) {
                e.toString()
            }
        }
        val actualResult = classUnderTest.resultList

        // Assert
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
        val mockedSpongeServer = MockedSpongeServer()
        val plugin = mockedSpongeServer.boot()
        val classUnderTest = TestEventListener()
        val clientConnectionEvent = Mockito.mock(ClientConnectionEvent.Join::class.java)
        Sponge.getEventManager().registerSuspendingListeners(plugin, classUnderTest)

        // Act
        runBlocking {
            try {
                Sponge.getEventManager().postSuspending(clientConnectionEvent, plugin, EventExecutionType.Consecutive).joinAll()
            } catch (e: Exception) {
                e.toString()
            }
        }
        val actualResult = classUnderTest.resultList

        // Assert
        assertEquals(1, actualResult[0])
        assertEquals(2, actualResult[1])
        assertEquals(3, actualResult[2])
    }

    private class TestEventListener {
        val resultList = ArrayList<Int>()

        @Listener(order = Order.FIRST)
        suspend fun onPlayerJoinEventLow(event: ClientConnectionEvent.Join) {
            delay(200)
            resultList.add(1)
        }

        @Listener
        fun onPlayerJoinEventNormal(event: ClientConnectionEvent.Join) {
            resultList.add(2)
        }

        @Listener(order = Order.LAST)
        suspend fun onPlayerJoinEventHigh(event: ClientConnectionEvent.Join) {
            delay(100)
            resultList.add(3)
        }
    }
}
