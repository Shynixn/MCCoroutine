package com.github.shynixn.mccoroutine.velocity.impl

import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.filter.AbstractFilter

class StartupLogFilter : AbstractFilter() {
    override fun filter(event: LogEvent): Filter.Result {
        val message = event.message.formattedMessage

        if (message.contains("but kotlin.coroutines.Continuation is invalid")) {
            return Filter.Result.DENY
        }

        return super.filter(event)
    }
}
