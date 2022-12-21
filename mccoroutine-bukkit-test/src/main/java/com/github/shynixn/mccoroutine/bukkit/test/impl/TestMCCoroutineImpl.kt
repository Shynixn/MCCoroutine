package com.github.shynixn.mccoroutine.bukkit.test.impl

import com.github.shynixn.mccoroutine.bukkit.CoroutineSession
import com.github.shynixn.mccoroutine.bukkit.MCCoroutine
import org.bukkit.plugin.Plugin

/**
 * An implementation which handles coroutines for unittests.
 */
internal class TestMCCoroutineImpl : MCCoroutine {
    private val items = HashMap<Plugin, TestCoroutineSessionImpl>()

    override fun getCoroutineSession(plugin: Plugin): CoroutineSession {
        if (!items.containsKey(plugin)) {
            val mcCoroutineConfiguration = TestMCCoroutineConfigurationImpl(plugin, this)
            val session = TestCoroutineSessionImpl(mcCoroutineConfiguration)
            items[plugin] = session
        }

        return items[plugin]!!
    }

    override fun disable(plugin: Plugin) {
        if (items.containsKey(plugin)) {
            val session = items.remove(plugin)!!
            session.dispose()
        }
    }
}
