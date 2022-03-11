package integrationtest

import com.github.shynixn.mccoroutine.launch
import com.github.shynixn.mccoroutine.service.EventServiceImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.SimpleCommandMap
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.SimplePluginManager
import org.bukkit.scheduler.BukkitScheduler
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import java.util.logging.Logger
import kotlin.test.assertEquals

class EventServiceTest {
    /**
     * Given a demo listener
     * When the demo listener is register and join event is called
     * then the join event should be called on the correct thread.
     */
    @Test
    fun registerSuspendListener_PlayerJoinEvent_ShouldCallEventWithCorrectThread() {
        // Arrange
        val plugin = Mockito.mock(Plugin::class.java)
        Mockito.`when`(plugin.isEnabled).thenReturn(true)
        val server = Mockito.mock(Server::class.java)
        val scheduler = Mockito.mock(BukkitScheduler::class.java)
        Mockito.`when`(plugin.server).thenReturn(server)
        Mockito.`when`(server.scheduler).thenReturn(scheduler)
        Mockito.`when`(server.pluginManager).thenReturn(Mockito.mock(PluginManager::class.java))
        val pluginManager = SimplePluginManager(server, Mockito.mock(SimpleCommandMap::class.java))
        Mockito.`when`(server.pluginManager).thenReturn(pluginManager)
        Mockito.`when`(server.logger).thenReturn(Logger.getGlobal())
        Mockito.`when`(plugin.description).thenReturn(Mockito.mock(PluginDescriptionFile::class.java))
        val classUnderTest = createWithDependencies(plugin)
        val demoListener = DemoListener()
        val expectedThreadId = Thread.currentThread().id
        if (Bukkit.getServer() == null) {
            Bukkit.setServer(server)
        }

        // Act
        classUnderTest.registerSuspendListener(demoListener)
        for (listener in PlayerJoinEvent.getHandlerList().registeredListeners) {
            listener.callEvent(PlayerJoinEvent(Mockito.mock(Player::class.java), ""))
        }

        // Assert
        assertEquals(expectedThreadId, demoListener.joinEventCalledId)
    }

    /**
     * Given a demo listener
     * When the demo listener is register and quit event is called
     * then the quit event should be called on the correct thread.
     */
    @Test
    fun registerSuspendListener_PlayerQuitEvent_ShouldCallEventWithCorrectThread() {
        // Arrange
        val plugin = Mockito.mock(Plugin::class.java)
        Mockito.`when`(plugin.isEnabled).thenReturn(true)
        val server = Mockito.mock(Server::class.java)
        val scheduler = Mockito.mock(BukkitScheduler::class.java)
        Mockito.`when`(plugin.server).thenReturn(server)
        Mockito.`when`(server.scheduler).thenReturn(scheduler)
        Mockito.`when`(server.pluginManager).thenReturn(Mockito.mock(PluginManager::class.java))
        plugin.launch { }
        val pluginManager = SimplePluginManager(server, Mockito.mock(SimpleCommandMap::class.java))
        Mockito.`when`(server.pluginManager).thenReturn(pluginManager)
        Mockito.`when`(server.logger).thenReturn(Logger.getGlobal())
        Mockito.`when`(plugin.description).thenReturn(Mockito.mock(PluginDescriptionFile::class.java))
        val classUnderTest = createWithDependencies(plugin)
        val demoListener = DemoListener()
        val expectedThreadId = Thread.currentThread().id
        if (Bukkit.getServer() == null) {
            Bukkit.setServer(server)
        }

        // Act
        classUnderTest.registerSuspendListener(demoListener)
        for (listener in PlayerQuitEvent.getHandlerList().registeredListeners) {
            listener.callEvent(PlayerQuitEvent(Mockito.mock(Player::class.java), ""))
        }

        // Assert
        assertEquals(expectedThreadId, demoListener.quitEventCalledId)
    }

    /**
     * Given a demo listener
     * When the demo listener is register and quit event is called
     * then the quit event should be called on the correct thread.
     */
    @Test
    fun registerSuspendListener_AsyncChatEvent_ShouldCallEventWithCorrectThread() {
        // Arrange
        val plugin = Mockito.mock(Plugin::class.java)
        Mockito.`when`(plugin.isEnabled).thenReturn(true)
        val server = Mockito.mock(Server::class.java)
        val scheduler = Mockito.mock(BukkitScheduler::class.java)
        Mockito.`when`(plugin.server).thenReturn(server)
        Mockito.`when`(server.scheduler).thenReturn(scheduler)
        Mockito.`when`(server.pluginManager).thenReturn(Mockito.mock(PluginManager::class.java))
        plugin.launch { }
        val pluginManager = SimplePluginManager(server, Mockito.mock(SimpleCommandMap::class.java))
        Mockito.`when`(server.pluginManager).thenReturn(pluginManager)
        Mockito.`when`(server.logger).thenReturn(Logger.getGlobal())
        Mockito.`when`(plugin.description).thenReturn(Mockito.mock(PluginDescriptionFile::class.java))
        val classUnderTest = createWithDependencies(plugin)
        val demoListener = DemoListener()
        var expectedThreadId: Long? = null
        if (Bukkit.getServer() == null) {
            Bukkit.setServer(server)
        }

        // Act
        classUnderTest.registerSuspendListener(demoListener)
        Thread({
            expectedThreadId = Thread.currentThread().id
            for (listener in AsyncPlayerChatEvent.getHandlerList().registeredListeners) {
                listener.callEvent(PlayerQuitEvent(Mockito.mock(Player::class.java), ""))
            }
        })

        Thread.sleep(1000)

        // Assert
        assertEquals(expectedThreadId, demoListener.asyncChatEventCalledId)
    }

    private fun createWithDependencies(plugin: Plugin): EventServiceImpl {
        return EventServiceImpl(plugin)
    }

    class DemoListener(
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
