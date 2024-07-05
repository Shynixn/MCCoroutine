package com.github.shynixn.mccoroutine.fabric

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory

/**
 * A Fabric event which is called when an exception is raised in one of the coroutines managed by MCCoroutine.
 * Cancelling this exception causes the error to not get logged and offers to possibility for custom logging.
 */
@FunctionalInterface
interface MCCoroutineExceptionEvent {
    companion object {
        val EVENT: Event<MCCoroutineExceptionEvent> =
            EventFactory.createArrayBacked(MCCoroutineExceptionEvent::class.java) { listeners ->
                object : MCCoroutineExceptionEvent {
                    /**
                     * Gets called from MCCoroutine with the occurred [throwable] in the given scope [entryPoint].
                     * @return True If the event should be cancelled (not get logged) or false if the event should not be cancelled.
                     */
                    override fun onMCCoroutineException(throwable: Throwable, entryPoint: Any): Boolean {
                        var cancel = false

                        for (listener in listeners) {
                            val result = listener.onMCCoroutineException(throwable, entryPoint)
                            if (result) {
                                cancel = true
                            }
                        }

                        return cancel
                    }
                }
            }
    }

    /**
     * Gets called from MCCoroutine with the occurred [throwable] in the given scope [entryPoint].
     * @return True If the event should be cancelled (not get logged) or false if the event should not be cancelled.
     */
    fun onMCCoroutineException(throwable: Throwable, entryPoint: Any): Boolean
}
