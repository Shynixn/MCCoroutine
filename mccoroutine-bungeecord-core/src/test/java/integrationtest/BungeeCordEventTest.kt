package integrationtest

import com.github.shynixn.mccoroutine.bungeecord.registerSuspendingListener
import helper.MockedBungeeCordServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.event.ServerDisconnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class BungeeCordEventTest {
    /**
     * Given a test listener
     * When the test listener is register and join event is called
     * then the join event should be called on the correct thread.
     */
    @Test
    fun registerSuspendListener_PlayerJoinEvent_ShouldCallEventWithCorrectThread() {
        // Arrange
        val server = MockedBungeeCordServer()
        val plugin = server.boot()
        val testListener = TestListener()

        // Act
        runBlocking(server.ionNettyDispatcher) {
            plugin.proxy.pluginManager.registerSuspendingListener(plugin, testListener)
            plugin.proxy.pluginManager.callEvent(PostLoginEvent(Mockito.mock(ProxiedPlayer::class.java)))
        }

        Thread.sleep(500)

        // Assert
        Assertions.assertEquals(server.ionNettyDispatcher.threadId, testListener.joinEventCalledId)
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
        val server = MockedBungeeCordServer()
        val plugin = server.boot()
        val testListener = TestListener()

        // Act
        runBlocking(server.ionNettyDispatcher) {
            plugin.proxy.pluginManager.registerSuspendingListener(plugin, testListener)
            plugin.proxy.pluginManager.callEvent(
                ServerDisconnectEvent(
                    Mockito.mock(ProxiedPlayer::class.java),
                    Mockito.mock(ServerInfo::class.java)
                )
            )
        }

        Thread.sleep(500)

        // Assert
        Assertions.assertEquals(server.ionNettyDispatcher.threadId, testListener.quitEventCalledId)
    }

    class TestListener(
        var joinEventCalledId: Long = 0L,
        var quitEventCalledId: Long = 0L,
        var asyncChatEventCalledId: Long = 0L,
        var leaveThreadId: Long = 0L
    ) : Listener {

        @EventHandler
        suspend fun onPlayerJoinEvent(event: PostLoginEvent) {
            joinEventCalledId = Thread.currentThread().id

            withContext(Dispatchers.IO) {
                Thread.sleep(100)
                asyncChatEventCalledId = Thread.currentThread().id
            }

            leaveThreadId = Thread.currentThread().id
        }

        @EventHandler
        fun onPlayerQuitEvent(event: ServerDisconnectEvent) {
            quitEventCalledId = Thread.currentThread().id
        }
    }
}
