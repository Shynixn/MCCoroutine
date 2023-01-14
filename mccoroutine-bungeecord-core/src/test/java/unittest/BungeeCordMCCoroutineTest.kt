package unittest

import com.github.shynixn.mccoroutine.bungeecord.impl.MCCoroutineImpl
import net.md_5.bungee.api.plugin.Plugin
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.concurrent.Executors

class BungeeCordMCCoroutineTest {
    /**
     * Given the same plugin
     * When getCoroutineSession is called multiple times
     * then the same session should be returned.
     */
    @Test
    fun getCoroutineSession_SamePlugin_ShouldReturnSameSession() {
        // Arrange
        val plugin = Mockito.mock(Plugin::class.java)
        val executors = Executors.newFixedThreadPool(4)
        Mockito.`when`(plugin.executorService).thenReturn(executors)
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
