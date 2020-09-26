package unittest

import com.github.shynixn.mccoroutine.contract.MCCoroutine
import com.github.shynixn.mccoroutine.listener.PluginListener
import helper.MockedMCCoroutine
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PluginListenerTest {
    /**
     * Given a plugin disable event of the current plugin.
     * When onPluginDisable is called
     * then the mccoroutine should get disabled.
     */
    @Test
    fun onPluginDisable_CurrentPlugin_ShouldDisableCoroutine() {
        // Arrange
        val mcCoroutine = MockedMCCoroutine()
        val plugin = Mockito.mock(Plugin::class.java)
        val classUnderTest = createWithDependencies(mcCoroutine, plugin)
        val pluginDisableEvent = PluginDisableEvent(plugin)

        // Act
        classUnderTest.onPluginDisable(pluginDisableEvent)

        // Assert
        assertTrue(mcCoroutine.disableCalled)
    }

    /**
     * Given a plugin disable event of another plugin.
     * When onPluginDisable is called
     * then the mccoroutine should not get disabled.
     */
    @Test
    fun onPluginDisable_OtherPlugin_ShouldNotDisableCoroutine() {
        // Arrange
        val mcCoroutine = MockedMCCoroutine()
        val classUnderTest = createWithDependencies(mcCoroutine)
        val pluginDisableEvent = PluginDisableEvent(Mockito.mock(Plugin::class.java))

        // Act
        classUnderTest.onPluginDisable(pluginDisableEvent)

        // Assert
        assertFalse(mcCoroutine.disableCalled)
    }

    /**
     * Given an ordinary playerJoinEvent.
     * When onPlayerJoinEvent is called
     * then the player should be protocol registered.
     */
    @Test
    fun onPlayerJoinEvent_OrdinaryPlayerJoin_ShouldRegisterProtocolService() {
        // Arrange
        val mcCoroutine = MockedMCCoroutine()
        var registerPlayerCalled = false
        Mockito.`when`(mcCoroutine.coroutineSession.protocolService.register(any(Player::class.java))).then {
            registerPlayerCalled = true
            Unit
        }
        val classUnderTest = createWithDependencies(mcCoroutine)
        val player = Mockito.mock(Player::class.java)
        val playerJoinEvent = PlayerJoinEvent(player, "")

        // Act
        classUnderTest.onPlayerJoinEvent(playerJoinEvent)

        // Assert
        assertTrue(registerPlayerCalled)
    }

    /**
     * Given an ordinary playerQuitEvent.
     * When onPlayerQuitEvent is called
     * then the player should not be protocol registered.
     */
    @Test
    fun onPlayerQuitEvent_OrdinaryPlayerQuit_ShouldUnRegisterProtocolService() {
        // Arrange
        val mcCoroutine = MockedMCCoroutine()
        var unRegisterCalled = false
        Mockito.`when`(mcCoroutine.coroutineSession.protocolService.unRegister(any(Player::class.java))).then {
            unRegisterCalled = true
            Unit
        }
        val classUnderTest = createWithDependencies(mcCoroutine)
        val player = Mockito.mock(Player::class.java)
        val playerQuitEvent = PlayerQuitEvent(player, "")

        // Act
        classUnderTest.onPlayerQuitEvent(playerQuitEvent)

        // Assert
        assertTrue(unRegisterCalled)
    }

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)

    private fun createWithDependencies(
        mcCoroutine: MCCoroutine,
        plugin: Plugin = Mockito.mock(Plugin::class.java)
    ): PluginListener {
        return PluginListener(mcCoroutine, plugin)
    }
}
