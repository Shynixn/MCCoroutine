package com.github.shynixn.mccoroutine.velocity

import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.plugin.PluginContainer

/**
 * A Velocity event which is called when an exception is raised in one of the coroutines managed by MCCoroutine.
 * Cancelling this exception causes the error to not get logged and offers to possibility for custom logging.
 */
class MCCoroutineExceptionEvent(
    /**
     * Plugin causing the exception.
     */
    val plugin: PluginContainer,
    /**
     * The exception to be logged.
     */
    val exception: Throwable
) : ResultedEvent<ResultedEvent.GenericResult> {
    private var eventResult = ResultedEvent.GenericResult.allowed()

    /**
     * Returns the result associated with this event.
     *
     * @return the result of this event
     */
    override fun getResult(): ResultedEvent.GenericResult {
        return eventResult
    }

    /**
     * Sets the result of this event. The result must be non-null.
     *
     * @param result the new result
     */
    override fun setResult(result: ResultedEvent.GenericResult) {
        this.eventResult = result
    }
}
