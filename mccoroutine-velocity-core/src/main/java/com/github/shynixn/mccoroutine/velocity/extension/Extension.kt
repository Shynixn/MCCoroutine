package com.github.shynixn.mccoroutine.velocity.extension

import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.PluginManager
import java.lang.reflect.Method

/**
 * Gets if the plugin is still enabled.
 */
internal fun PluginContainer.isPluginEnabled(pluginManager: PluginManager): Boolean {
    val result = pluginManager.isLoaded(this.description.id)
    return result
}

/**
 * Internal reflection suspend.
 */
internal suspend fun Method.invokeSuspend(obj: Any, vararg args: Any?): Any? =
    kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn { cont ->
        invoke(obj, *args, cont)
    }

