package unittest

import com.github.shynixn.mccoroutine.sponge.impl.MCCoroutineImpl
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.spongepowered.api.plugin.PluginContainer

class SpongeMCCoroutineTest {
    /**
     * Given the same plugin
     * When getCoroutineSession is called multiple times
     * then the same session should be returned.
     */
    @Test
    fun getCoroutineSession_SamePlugin_ShouldReturnSameSession() {
        // Arrange
        val plugin = Mockito.mock(PluginContainer::class.java)
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
