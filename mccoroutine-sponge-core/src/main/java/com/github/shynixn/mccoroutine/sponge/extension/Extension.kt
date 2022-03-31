package com.github.shynixn.mccoroutine.sponge.extension

import org.spongepowered.api.plugin.PluginContainer
import java.lang.reflect.Method

/**
 * Internal reflection suspend.
 */
internal suspend fun Method.invokeSuspend(obj: Any, vararg args: Any?): Any? =
    kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn { cont ->
        invoke(obj, *args, cont)
    }

/**
 * Gets if the plugin is still enabled.
 */
internal val PluginContainer.isEnabled : Boolean
    get() {
        return true
    }
