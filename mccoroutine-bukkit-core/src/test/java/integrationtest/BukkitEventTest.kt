package integrationtest

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.service.EventServiceImpl
import helper.MockedBukkitServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class BukkitEventTest {
    /**
     * Given a test listener
     * When the test listener is register and join event is called
     * then the join event should be called on the correct thread.
     */
    @Test
    fun registerSuspendListener_PlayerJoinEvent_ShouldCallEventWithCorrectThread() {
        // Arrange
        val server = MockedBukkitServer()
        val plugin = server.boot()
        val classUnderTest = createWithDependencies(plugin)
        val testListener = TestListener()

        // Act
        classUnderTest.registerSuspendListener(testListener)
        runBlocking(plugin.minecraftDispatcher) {
            for (listener in PlayerJoinEvent.getHandlerList().registeredListeners) {
                listener.callEvent(PlayerJoinEvent(Mockito.mock(Player::class.java), ""))
            }
        }

        // Assert
        assertEquals(server.mainThreadId, testListener.joinEventCalledId)
    }

    /**
     * Given a test listener
     * When the test listener is register and quit event is called
     * then the quit event should be called on the correct thread.
     */
    @Test
    fun registerSuspendListener_PlayerQuitEvent_ShouldCallEventWithCorrectThread() {
        // Arrange
        val server = MockedBukkitServer()
        val plugin = server.boot()
        val classUnderTest = createWithDependencies(plugin)
        val testListener = TestListener()

        // Act
        classUnderTest.registerSuspendListener(testListener)
        runBlocking(plugin.minecraftDispatcher) {
            for (listener in PlayerQuitEvent.getHandlerList().registeredListeners) {
                listener.callEvent(PlayerQuitEvent(Mockito.mock(Player::class.java), ""))
            }
        }

        // Assert
        assertEquals(server.mainThreadId, testListener.quitEventCalledId)
    }

    /**
     * Given a test listener
     * When the test listener is register and quit event is called
     * then the quit event should be called on the correct thread.
     */
    @Test
    fun registerSuspendListener_AsyncChatEvent_ShouldCallEventWithCorrectThread() {
        // Arrange
        val server = MockedBukkitServer()
        val plugin = server.boot()
        val classUnderTest = createWithDependencies(plugin)
        val testListener = TestListener()

        // Act
        classUnderTest.registerSuspendListener(testListener)
        runBlocking(plugin.asyncDispatcher) {
            for (listener in AsyncPlayerChatEvent.getHandlerList().registeredListeners) {
                listener.callEvent(PlayerQuitEvent(Mockito.mock(Player::class.java), ""))
            }
        }

        // Assert
        assertNotEquals(server.mainThreadId, testListener.asyncChatEventCalledId)
    }

    private fun createWithDependencies(plugin: Plugin): EventServiceImpl {
        return EventServiceImpl(plugin)
    }

    class TestListener(
        var joinEventCalledId: Long? = null,
        var quitEventCalledId: Long? = null,
        var asyncChatEventCalledId: Long? = null
    ) : Listener {

        @EventHandler
        suspend fun onPlayerJoinEvent(event: PlayerJoinEvent) {
            joinEventCalledId = Thread.currentThread().id

            withContext(Dispatchers.IO) {
                Thread.sleep(500)
            }
        }

        @EventHandler
        fun onPlayerQuitEvent(event: PlayerQuitEvent) {
            quitEventCalledId = Thread.currentThread().id
        }

        @EventHandler
        suspend fun onPlayerAsyncChatEvent(event: AsyncPlayerChatEvent) {
            asyncChatEventCalledId = Thread.currentThread().id

            withContext(Dispatchers.IO) {
                Thread.sleep(500)
            }
        }
    }
}
