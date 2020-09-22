package com.github.shynixn.mccoroutine.entity

import com.github.shynixn.mccoroutine.contract.CoroutineSession
import kotlinx.coroutines.CoroutineScope

class CoroutineSessionImpl : CoroutineSession {
    /**
     * Launches the given function on the Minecraft Thread and handles
     * coroutine scopes correctly.
     */
    override fun launchOnMinecraft(f: suspend CoroutineScope.() -> Unit) {
        TODO("Not yet implemented")
    }
}
