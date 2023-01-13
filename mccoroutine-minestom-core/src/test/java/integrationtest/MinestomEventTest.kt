package integrationtest

import com.github.shynixn.mccoroutine.minestom.addSuspendingListener
import helper.MockedMinestomServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerLoginEvent
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class MinestomEventTest {
    /**
     * Given a test listener
     * When the test listener is register and join event is called
     * then the join event should be called on the correct thread.
     */
    @Test
    fun registerSuspendListener_PlayerJoinEvent_ShouldCallEventWithCorrectThread() {
        // Arrange
        val server = MockedMinestomServer()
        val extension = server.boot()
        val testListener = TestListener()
        var unitTestThreadId: Long

        // Act
        runBlocking {
            unitTestThreadId = Thread.currentThread().id
            MinecraftServer.getGlobalEventHandler()
                .addSuspendingListener(extension, PlayerLoginEvent::class.java) { e ->
                    testListener.onPlayerJoinEvent(e)
                }
            MinecraftServer.getGlobalEventHandler().call(PlayerLoginEvent(Mockito.mock(Player::class.java)))
        }

        Thread.sleep(500)

        // Assert
        Assertions.assertNotEquals(unitTestThreadId, testListener.joinEventCalledId)
        Assertions.assertNotEquals(unitTestThreadId, testListener.asyncChatEventCalledId)
        Assertions.assertNotEquals(unitTestThreadId, testListener.leaveThreadId)
        Assertions.assertEquals(testListener.joinEventCalledId, testListener.leaveThreadId)
        Assertions.assertNotEquals(testListener.asyncChatEventCalledId, testListener.leaveThreadId)
    }

    /**
     * Given a test listener
     * When the test listener is register and quit event is called
     * then the quit event should be called on the correct thread.
     */
    @Test
    fun registerSuspendListener_PlayerQuitEvent_ShouldCallEventWithCorrectThread() {
        // Arrange
        val server = MockedMinestomServer()
        val extension = server.boot()
        val testListener = TestListener()
        var unitTestThreadId: Long

        // Act
        runBlocking {
            unitTestThreadId = Thread.currentThread().id
            MinecraftServer.getGlobalEventHandler()
                .addSuspendingListener(extension, PlayerDisconnectEvent::class.java) { e ->
                    testListener.onPlayerQuitEvent(e)
                }
            MinecraftServer.getGlobalEventHandler().call(PlayerLoginEvent(Mockito.mock(Player::class.java)))
        }

        Thread.sleep(500)

        // Assert
        Assertions.assertNotEquals(unitTestThreadId, testListener.quitEventCalledId)
    }

    class TestListener(
        var joinEventCalledId: Long = 0L,
        var quitEventCalledId: Long = 0L,
        var asyncChatEventCalledId: Long = 0L,
        var leaveThreadId: Long = 0L
    ) {

        suspend fun onPlayerJoinEvent(event: PlayerLoginEvent) {
            joinEventCalledId = Thread.currentThread().id

            withContext(Dispatchers.IO) {
                Thread.sleep(100)
                asyncChatEventCalledId = Thread.currentThread().id
            }

            leaveThreadId = Thread.currentThread().id
        }

        fun onPlayerQuitEvent(event: PlayerDisconnectEvent) {
            quitEventCalledId = Thread.currentThread().id
        }
    }
}
