package integrationtest

import com.github.shynixn.mccoroutine.sponge.minecraftDispatcher
import com.github.shynixn.mccoroutine.sponge.registerSuspendingListeners
import helper.MockedSpongeServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.spongepowered.api.Sponge
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.network.ClientConnectionEvent

class SpongeEventTest {
    /**
     * Given a test listener
     * When the test listener is register and join event is called
     * then the join event should be called on the correct thread.
     */
    @Test
    fun registerSuspendListener_PlayerJoinEvent_ShouldCallEventWithCorrectThread() {
        // Arrange
        val server = MockedSpongeServer()
        val plugin = server.boot()
        val testListener = TestListener()

        // Act
        runBlocking(plugin.minecraftDispatcher) {
            Sponge.getEventManager().registerSuspendingListeners(plugin, testListener)
            testListener.onPlayerJoinEvent(Mockito.mock(ClientConnectionEvent.Join::class.java))
        }

        // Assert
        Assertions.assertEquals(server.mainThreadId, testListener.joinEventCalledId)
        Assertions.assertNotEquals(server.mainThreadId, testListener.asyncCalledId)
        Assertions.assertEquals(server.mainThreadId, testListener.leaveId)
    }

    /**
     * Given a test listener
     * When the test listener is register and quit event is called
     * then the quit event should be called on the correct thread.
     */
    @Test
    fun registerSuspendListener_PlayerQuitEvent_ShouldCallEventWithCorrectThread() {
        // Arrange
        val server = MockedSpongeServer()
        val plugin = server.boot()
        val testListener = TestListener()

        // Act
        runBlocking(plugin.minecraftDispatcher) {
            Sponge.getEventManager().registerSuspendingListeners(plugin, testListener)
            testListener.onPlayerQuitEvent(Mockito.mock(ClientConnectionEvent.Disconnect::class.java))
        }

        // Assert
        Assertions.assertEquals(server.mainThreadId, testListener.quitEventCalledId)
    }

    class TestListener(
        var joinEventCalledId: Long? = null,
        var quitEventCalledId: Long? = null,
        var asyncCalledId: Long? = null,
        var leaveId: Long? = null
    ) {

        @Listener
        suspend fun onPlayerJoinEvent(event: ClientConnectionEvent.Join) {
            joinEventCalledId = Thread.currentThread().id

            withContext(Dispatchers.IO) {
                Thread.sleep(500)
                asyncCalledId = Thread.currentThread().id
            }

            leaveId = Thread.currentThread().id
        }

        @Listener
        fun onPlayerQuitEvent(event: ClientConnectionEvent.Disconnect) {
            quitEventCalledId = Thread.currentThread().id
        }
    }
}
