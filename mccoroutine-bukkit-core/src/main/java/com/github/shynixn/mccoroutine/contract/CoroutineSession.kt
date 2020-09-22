package com.github.shynixn.mccoroutine.contract

import kotlinx.coroutines.CoroutineScope

interface CoroutineSession {
    /**
     * Launches the given function on the Minecraft Thread and handles
     * coroutine scopes correctly.
     */
    fun launchOnMinecraft(f: suspend CoroutineScope.() -> Unit)
}
