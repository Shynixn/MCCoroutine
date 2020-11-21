package com.github.shynixn.mccoroutine.service

import com.github.shynixn.mccoroutine.contract.CoroutineSession
import com.github.shynixn.mccoroutine.contract.EventService
import com.github.shynixn.mccoroutine.extension.invokeSuspend
import kotlinx.coroutines.Dispatchers
import org.bukkit.Warning
import org.bukkit.event.*
import org.bukkit.plugin.*
import java.lang.Deprecated
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.logging.Level
import kotlin.IllegalArgumentException
import kotlin.String
import kotlin.Throwable

internal class EventServiceImpl(private val plugin: Plugin, private val coroutineSession: CoroutineSession) :
    EventService {
    /**
     * Registers a suspend listener.
     */
    override fun registerSuspendListener(listener: Listener) {
        val registeredListeners = createCoroutineListener(listener, plugin)

        val method = SimplePluginManager::class.java
            .getDeclaredMethod("getEventListeners", Class::class.java)
        method.isAccessible = true

        for (entry in registeredListeners.entries) {
            val clazz = entry.key
            val handlerList = method.invoke(plugin.server.pluginManager, clazz) as HandlerList
            handlerList.registerAll(entry.value as MutableCollection<RegisteredListener>)
        }
    }

    /**
     * Creates a listener according to the spigot implementation.
     */
    private fun createCoroutineListener(
        listener: Listener,
        plugin: Plugin
    ): Map<Class<*>, MutableSet<RegisteredListener>> {
        val eventMethods = HashSet<Method>()

        try {
            // Adds public methods of the current class and inherited classes
            eventMethods.addAll(listener.javaClass.methods)
            // Adds all methods of the current class
            eventMethods.addAll(listener.javaClass.declaredMethods)
        } catch (e: NoClassDefFoundError) {
            plugin.logger.severe("Plugin " + plugin.description.fullName + " has failed to register events for " + listener.javaClass + " because " + e.message + " does not exist.")
            return emptyMap()
        }

        val result = mutableMapOf<Class<*>, MutableSet<RegisteredListener>>()

        for (method in eventMethods) {
            val annotation = method.getAnnotation(EventHandler::class.java)

            if (annotation == null || method.isBridge || method.isSynthetic) {
                continue
            }

            val eventClass = method.parameterTypes[0].asSubclass(Event::class.java)
            method.isAccessible = true

            if (!result.containsKey(eventClass)) {
                result[eventClass] = HashSet()
            }

            var clazz: Class<*> = eventClass

            while (Event::class.java.isAssignableFrom(clazz)) {
                if (clazz.getAnnotation(Deprecated::class.java) == null) {
                    clazz = clazz.superclass
                    continue
                }

                val warning = clazz.getAnnotation(Warning::class.java)
                val warningState = plugin.server.warningState

                if (!warningState.printFor(warning)) {
                    break
                }

                plugin.logger.log(
                    Level.WARNING,
                    """"%s" has registered a listener for %s on method "%s", but the event is Deprecated. "%s"; please notify the authors %s.""".format(
                        plugin.description.fullName,
                        clazz.name,
                        method.toGenericString(),
                        if (warning?.reason?.isNotEmpty() == true) warning.reason else "Server performance will be affected",
                        plugin.description.authors.toTypedArray().contentToString()
                    ),
                    if (warningState == Warning.WarningState.ON) {
                        AuthorNagException(null as String?)
                    } else null
                )
            }

            val executor = createEventExecutor(eventClass, method)
            result[eventClass]!!.add(
                RegisteredListener(
                    listener,
                    executor,
                    annotation.priority,
                    plugin,
                    annotation.ignoreCancelled
                )
            )
        }

        return result
    }

    /**
     * Creates a single event executor.
     */
    private fun createEventExecutor(
        eventClass: Class<*>,
        method: Method
    ): EventExecutor {
        return EventExecutor { listener, event ->
            try {
                if (eventClass.isAssignableFrom(event.javaClass)) {
                    val isAsync = event.isAsynchronous

                    val dispatcher = if (isAsync) {
                        // Unconfined because async events should be supported too.
                        Dispatchers.Unconfined
                    } else {
                        coroutineSession.dispatcherMinecraft
                    }

                    coroutineSession.launch(dispatcher) {
                        try {
                            // Try as suspension function.
                            method.invokeSuspend(listener, event)
                        } catch (e: IllegalArgumentException) {
                            // Try as ordinary function.
                            method.invoke(listener, event)
                        }
                    }
                }
            } catch (var4: InvocationTargetException) {
                throw EventException(var4.cause)
            } catch (var5: Throwable) {
                throw EventException(var5)
            }
        }
    }
}
