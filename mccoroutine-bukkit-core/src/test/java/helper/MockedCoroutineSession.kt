package helper

import com.github.shynixn.mccoroutine.contract.CommandService
import com.github.shynixn.mccoroutine.contract.CoroutineSession
import com.github.shynixn.mccoroutine.contract.EventService
import com.github.shynixn.mccoroutine.contract.WakeUpBlockService
import kotlinx.coroutines.*
import org.mockito.Mockito
import kotlin.coroutines.CoroutineContext

class MockedCoroutineSession : CoroutineSession {
    var disposeCalled = false

    /**
     * Gets the scope.
     */
    override var scope: kotlinx.coroutines.CoroutineScope = Mockito.mock(CoroutineScope::class.java)

    /**
     * Gets the event service.
     */
    override var eventService: EventService = Mockito.mock(EventService::class.java)

    /**
     * Gets the command service.
     */
    override var commandService: CommandService = Mockito.mock(CommandService::class.java)

    /**
     * Gets the wakeup service.
     */
    override val wakeUpBlockService: WakeUpBlockService = Mockito.mock(WakeUpBlockService::class.java)

    /**
     * Gets the minecraft dispatcher.
     */
    override var dispatcherMinecraft: CoroutineContext = Dispatchers.Unconfined

    /**
     * Gets the async dispatcher.
     */
    override var dispatcherAsync: CoroutineContext = Mockito.mock(CoroutineContext::class.java)

    /**
     * Disposes the session.
     */
    override fun dispose() {
        disposeCalled = true
    }

    /**
     * Launches the given function on the plugin coroutine scope.
     */
    override fun launch(dispatcher: CoroutineContext, f: suspend kotlinx.coroutines.CoroutineScope.() -> Unit) : Job {
        return GlobalScope.launch(dispatcher) {
            f.invoke(this)
        }
    }
}
