package com.github.shynixn.mccoroutine.bukkit.extension

import org.bukkit.plugin.Plugin
import java.lang.reflect.Method

/**
 * Internal reflection suspend.
 */
internal suspend fun Method.invokeSuspend(obj: Any, vararg args: Any?): Any? =
    kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn { cont ->
        invoke(obj, *args, cont)
    }


private var serverVersionInternal: String? = null

/**
 * Gets the server NMS version.
 */
internal val Plugin.serverVersion: String
    get() {
        if (serverVersionInternal == null) {
            serverVersionInternal = server.javaClass.getPackage().name.replace(".", ",").split(",")[3]
        }

        return serverVersionInternal!!
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
