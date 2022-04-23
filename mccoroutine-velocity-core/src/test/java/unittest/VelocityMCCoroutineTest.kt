package unittest

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.github.shynixn.mccoroutine.velocity.impl.MCCoroutineImpl
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.PluginDescription
import com.velocitypowered.api.plugin.PluginManager
import com.velocitypowered.api.proxy.ProxyServer
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.slf4j.Logger
import kotlin.test.assertEquals

class VelocityMCCoroutineTest {
    /**
     * Given the same plugin
     * When getCoroutineSession is called multiple times
     * then the same session should be returned.
     */
    @Test
    fun getCoroutineSession_SamePlugin_ShouldReturnSameSession() {
        // Arrange
        val plugin = Mockito.mock(PluginContainer::class.java)
        val server = Mockito.mock(ProxyServer::class.java)
        val pluginManager = Mockito.mock(PluginManager::class.java)
        Mockito.`when`(server.pluginManager).thenReturn(pluginManager)
        val pluginDescription = Mockito.mock(PluginDescription::class.java)
        Mockito.`when`(pluginDescription.id).thenReturn("test")
        Mockito.`when`(plugin.description).thenReturn(pluginDescription)
        Mockito.`when`(pluginManager.isLoaded(Mockito.anyString())).thenReturn(true)
        val suspendingPluginContainer = SuspendingPluginContainer(plugin, server, Mockito.mock(Logger::class.java))
        val classUnderTest = createWithDependencies()

        // Act
        classUnderTest.setupCoroutineSession(plugin, suspendingPluginContainer)
        val session1 = classUnderTest.getCoroutineSession(plugin)
        val session2 = classUnderTest.getCoroutineSession(plugin)

        // Assert
        assertEquals(session1, session2)
    }

    private fun createWithDependencies(): MCCoroutineImpl {
        return MCCoroutineImpl()
    }
}
