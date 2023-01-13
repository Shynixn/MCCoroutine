package unittest

import com.github.shynixn.mccoroutine.bukkit.impl.MCCoroutineImpl
import org.bukkit.Server
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class BukkitMCCoroutineTest {
    /**
     * Given the same plugin
     * When getCoroutineSession is called multiple times
     * then the same session should be returned.
     */
    @Test
    fun getCoroutineSession_SamePlugin_ShouldReturnSameSession() {
        // Arrange
        val plugin = Mockito.mock(Plugin::class.java)
        val server = Mockito.mock(Server::class.java)
        Mockito.`when`(server.onlinePlayers).thenReturn(emptyList())
        Mockito.`when`(plugin.isEnabled).thenReturn(true)
        Mockito.`when`(plugin.server).thenReturn(server)
        Mockito.`when`(server.pluginManager).thenReturn(Mockito.mock(PluginManager::class.java))
        val classUnderTest = createWithDependencies()

        // Act
        val session1 = classUnderTest.getCoroutineSession(plugin)
        val session2 = classUnderTest.getCoroutineSession(plugin)

        // Assert
        Assertions.assertEquals(session1, session2)
    }

    private fun createWithDependencies(): MCCoroutineImpl {
        return MCCoroutineImpl()
    }
}
