package integrationtest

import com.github.shynixn.mccoroutine.velocity.registerSuspend
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.proxy.Player
import helper.MockedVelocityServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class VelocityEventTest {
    /**
     * Given a test listener
     * When the test listener is register and join event is called
     * then the join event should be called on the correct thread.
     */
    @Test
    fun registerSuspendListener_PlayerJoinEvent_ShouldCallEventWithCorrectThread() {
        // Arrange
        val server = MockedVelocityServer()
        val plugin = server.boot()
        val testListener = TestListener()

        // Act
        runBlocking(server.ionNettyDispatcher) {
            server.proxyServer.eventManager.registerSuspend(plugin, testListener)
            server.proxyServer.eventManager.fire(PostLoginEvent(Mockito.mock(Player::class.java))).get()
        }

        Thread.sleep(500)

        // Assert
        Assertions.assertNotEquals(server.ionNettyDispatcher.threadId, testListener.joinEventCalledId)
        Assertions.assertNotEquals(server.ionNettyDispatcher.threadId, testListener.asyncChatEventCalledId)
        Assertions.assertNotEquals(server.ionNettyDispatcher.threadId, testListener.leaveThreadId)
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
        val server = MockedVelocityServer()
        val plugin = server.boot()
        val testListener = TestListener()

        // Act
        runBlocking(server.ionNettyDispatcher) {
            server.proxyServer.eventManager.registerSuspend(plugin, testListener)
            server.proxyServer.eventManager.fire(
                DisconnectEvent(
                    Mockito.mock(Player::class.java),
                    DisconnectEvent.LoginStatus.PRE_SERVER_JOIN
                )
            )
        }

        Thread.sleep(500)

        // Assert
        Assertions.assertNotEquals(server.ionNettyDispatcher.threadId, testListener.quitEventCalledId)
    }

    class TestListener(
        var joinEventCalledId: Long = 0L,
        var quitEventCalledId: Long = 0L,
        var asyncChatEventCalledId: Long = 0L,
        var leaveThreadId: Long = 0L
    ) {

        @Subscribe
        suspend fun onPlayerJoinEvent(event: PostLoginEvent) {
            joinEventCalledId = Thread.currentThread().id

            withContext(Dispatchers.IO) {
                Thread.sleep(100)
                asyncChatEventCalledId = Thread.currentThread().id
            }

            leaveThreadId = Thread.currentThread().id
        }

        @Subscribe
        fun onPlayerQuitEvent(event: DisconnectEvent) {
            quitEventCalledId = Thread.currentThread().id
        }
    }
}
