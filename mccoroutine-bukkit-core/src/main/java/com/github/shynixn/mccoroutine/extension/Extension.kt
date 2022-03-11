package com.github.shynixn.mccoroutine.extension

import com.github.shynixn.mccoroutine.serverVersion
import org.bukkit.plugin.Plugin
import java.lang.reflect.Method

/**
 * Internal reflection suspend.
 */
internal suspend fun Method.invokeSuspend(obj: Any, vararg args: Any?): Any? =
    kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn { cont ->
        invoke(obj, *args, cont)
    }


/**
 * Finds the version compatible class.
 */
internal fun Plugin.findClazz(name: String): Class<*> {
    return Class.forName(
        name.replace(
            "VERSION",
            serverVersion
        )
    )
}
