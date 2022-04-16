package unittest

import com.github.shynixn.mccoroutine.bukkit.MCCoroutine
import com.github.shynixn.mccoroutine.bukkit.impl.CoroutineSessionImpl
import com.github.shynixn.mccoroutine.bukkit.listener.PluginListener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BukkitPluginListenerTest {
    /**
     * Given a plugin disable event of the current plugin.
     * When onPluginDisable is called
     * Then MCCoroutine should get disabled.
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
     * hen MCCoroutine should not get disabled.
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

    private fun createWithDependencies(
        mcCoroutine: MCCoroutine,
        plugin: Plugin = Mockito.mock(Plugin::class.java)
    ): PluginListener {
        return PluginListener(mcCoroutine, plugin)
    }

    private class MockedMCCoroutine : MCCoroutine {
        var disableCalled = false
        override fun getCoroutineSession(plugin: Plugin): CoroutineSessionImpl {
            return Mockito.mock(CoroutineSessionImpl::class.java)
        }

        override fun disable(plugin: Plugin) {
            disableCalled = true
        }
    }
}
