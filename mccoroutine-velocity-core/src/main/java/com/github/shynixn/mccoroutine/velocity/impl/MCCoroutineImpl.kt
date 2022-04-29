package com.github.shynixn.mccoroutine.velocity.impl

import com.github.shynixn.mccoroutine.velocity.CoroutineSession
import com.github.shynixn.mccoroutine.velocity.MCCoroutine
import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.velocitypowered.api.plugin.PluginContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.logging.log4j.core.Logger
import java.util.concurrent.ConcurrentHashMap

class MCCoroutineImpl : MCCoroutine {
    private val items = ConcurrentHashMap<Any, CoroutineSessionImpl>()
    private val filterSessions = ConcurrentHashMap<PluginContainer, StartupLogFilter>()
    private var logger: Logger? = null

    /**
     * Get coroutine session for the given plugin.
     */
    override fun getCoroutineSession(plugin: Any): CoroutineSession {
        val pluginInstance = if (plugin is PluginContainer) {
            plugin.instance.get()
        } else {
            plugin
        }

        if (!items.containsKey(pluginInstance)) {
            throw IllegalArgumentException("Inject SuspendingPluginContainer into your plugin class to boot MCCoroutine!")
        }

        return items[pluginInstance]!!
    }

    /**
     * Configures the suspending plugin container with the real plugin Container.
     */
    override fun setupCoroutineSession(
        pluginInstance: Any,
        pluginContainer: PluginContainer,
        suspendingPluginContainer: SuspendingPluginContainer
    ) {
        if (items.contains(pluginInstance)) {
            return
        }

        // Velocity does not have any static API functions. Therefore, we need to link plugin and suspending plugin manually.
        items[pluginInstance] = CoroutineSessionImpl(pluginContainer, suspendingPluginContainer)

        // ReEnable logging.
        val session = getCoroutineSession(pluginInstance)
        val pluginManager = suspendingPluginContainer.server.pluginManager
        session.scope.launch(session.dispatcherVelocity) {
            // Once the plugin is enabled, the filter is removed again to avoid any conflicts.
            while (!pluginManager.isLoaded(pluginContainer.description.id)) {
                delay(5000)
            }

            if (filterSessions.containsKey(pluginContainer)) {
                val filter = filterSessions.remove(pluginContainer)

                if (filter != null) {
                    logger?.get()?.removeFilter(filter)
                }
            }
        }
    }

    /**
     * Disables coroutine for the given plugin.
     */
    override fun disable(plugin: PluginContainer) {
        if (!items.containsKey(plugin)) {
            return
        }

        val session = items[plugin]!!
        session.dispose()
        items.remove(plugin)
    }

    /**
     * Disables logging false positives.
     * Hooks into Log4J to temporarily add a filter until the plugin is enabled.
     */
    override fun disableLogging(plugin: PluginContainer, suspendingPluginContainer: SuspendingPluginContainer) {
        try {
            val clazz = Class.forName("com.velocitypowered.proxy.VelocityServer")
            val loggerField = clazz.getDeclaredField("logger")
            loggerField.isAccessible = true
            val filter = StartupLogFilter()
            this.logger = loggerField.get(null) as Logger
            this.logger!!.get().addFilter(filter)
            this.filterSessions[plugin] = filter
        } catch (e: Exception) {
            // The user may have different logging settings, but then we do not care about disabling them.
            suspendingPluginContainer.logger.warn("MCCoroutine failed to filter false positive warnings during startup.")
        }
    }
}
