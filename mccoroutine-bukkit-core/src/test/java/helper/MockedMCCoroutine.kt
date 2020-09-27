package helper

import com.github.shynixn.mccoroutine.contract.CoroutineSession
import com.github.shynixn.mccoroutine.contract.MCCoroutine
import org.bukkit.plugin.Plugin

class MockedMCCoroutine : MCCoroutine {
    var coroutineSession: MockedCoroutineSession = MockedCoroutineSession()
    var disableCalled = false

    /**
     * Get coroutine session for the given plugin.
     */
    override fun getCoroutineSession(plugin: Plugin): CoroutineSession {
        return coroutineSession!!
    }

    /**
     * Disables coroutine for the given plugin.
     */
    override fun disable(plugin: Plugin) {
        disableCalled = true
    }
}
