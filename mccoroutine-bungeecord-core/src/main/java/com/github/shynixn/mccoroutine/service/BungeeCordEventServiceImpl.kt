package com.github.shynixn.mccoroutine.service

import com.github.shynixn.mccoroutine.contract.CoroutineSession
import com.github.shynixn.mccoroutine.contract.EventService
import com.github.shynixn.mccoroutine.extension.invokeSuspend
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Multimap
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.PluginManager
import net.md_5.bungee.event.EventBus
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventHandlerMethod
import java.lang.reflect.Method
import java.util.concurrent.locks.Lock
import java.util.logging.Level

internal class BungeeCordEventServiceImpl(private val plugin: Plugin, private val coroutineSession: CoroutineSession) :
    EventService {
    /**
     * Registers a suspend listener.
     * The reflection calls are only present at startup which does not rise any performance concerns later on.
     */
    override fun registerSuspendListener(listener: Listener) {
        // Hook into the BungeeCord internal fields.
        val eventBusField = PluginManager::class.java.getDeclaredField("eventBus")
        eventBusField.isAccessible = true
        val eventBus = eventBusField.get(plugin.proxy.pluginManager) as EventBus

        val listenersByPluginField = PluginManager::class.java.getDeclaredField("listenersByPlugin")
        listenersByPluginField.isAccessible = true
        val listenersByPlugin = listenersByPluginField.get(plugin.proxy.pluginManager) as Multimap<Plugin, Listener>

        val lockField = EventBus::class.java.getDeclaredField("lock")
        lockField.isAccessible = true
        val lock = lockField.get(eventBus) as Lock

        val byListenerAndPriorityField = EventBus::class.java.getDeclaredField("byListenerAndPriority")
        byListenerAndPriorityField.isAccessible = true
        val byListenerAndPriority =
            byListenerAndPriorityField.get(eventBus) as MutableMap<Class<*>, MutableMap<Byte, MutableMap<Any, Array<Method>>>>

        val byEventBakedField = EventBus::class.java.getDeclaredField("byEventBaked")
        byEventBakedField.isAccessible = true
        val byEventBaked = byEventBakedField.get(eventBus) as MutableMap<Class<*>, Array<EventHandlerMethod>>

        // Register listener in EventHandler.
        val handler: Map<Class<*>, Map<Byte, Set<Method>>> = findHandlers(listener)
        lock.lock()
        try {
            for ((key, value) in handler) {
                var prioritiesMap: MutableMap<Byte, MutableMap<Any, Array<Method>>>? = byListenerAndPriority.get(key)
                if (prioritiesMap == null) {
                    prioritiesMap = java.util.HashMap()
                    byListenerAndPriority[key] = prioritiesMap
                }
                for ((key1, value1) in value) {
                    var currentPriorityMap = prioritiesMap[key1]
                    if (currentPriorityMap == null) {
                        currentPriorityMap = java.util.HashMap()
                        prioritiesMap[key1] = currentPriorityMap
                    }
                    currentPriorityMap[listener] = value1.toTypedArray<Method>()
                }
                bakeHandlers(key, byListenerAndPriority, byEventBaked)
            }
        } finally {
            lock.unlock()
        }

        // Register listener in PluginManager.
        listenersByPlugin.put(plugin, listener)
    }

    /**
     * Equivalent to PluginManager.findHandlers, just with suspend support.
     */
    private fun findHandlers(listener: Any): Map<Class<*>, MutableMap<Byte, MutableSet<Method>>> {
        val handler: MutableMap<Class<*>, MutableMap<Byte, MutableSet<Method>>> = HashMap()
        val methods: Set<Method> =
            ImmutableSet.builder<Method>().add(*listener.javaClass.methods).add(*listener.javaClass.declaredMethods)
                .build()
        for (m in methods) {
            val annotation = m.getAnnotation(EventHandler::class.java)

            if (annotation != null) {
                val params = m.parameterTypes
                // Suspend adds a second hidden parameter.
                if (params.size != 1 && params.size != 2) {
                    plugin.logger.log(
                        Level.INFO, "Method {0} in class {1} annotated with {2} does not have single argument", arrayOf(
                            m, listener.javaClass, annotation
                        )
                    )
                    continue
                }
                var prioritiesMap = handler[params[0]]
                if (prioritiesMap == null) {
                    prioritiesMap = HashMap()
                    handler[params[0]] = prioritiesMap
                }
                var priority = prioritiesMap[annotation.priority]
                if (priority == null) {
                    priority = HashSet()
                    prioritiesMap[annotation.priority] = priority
                }
                priority.add(m)
            }
        }

        return handler
    }

    /**
     * Equivalent to EventBus.bakeHandlers with suspend support.
     */
    private fun bakeHandlers(
        eventClass: Class<*>,
        byListenerAndPriority: MutableMap<Class<*>, MutableMap<Byte, MutableMap<Any, Array<Method>>>>,
        byEventBaked: MutableMap<Class<*>, Array<EventHandlerMethod>>
    ) {
        val handlersByPriority: MutableMap<Byte, MutableMap<Any, Array<Method>>>? =
            byListenerAndPriority[eventClass]
        if (handlersByPriority != null) {
            val handlersList: MutableList<EventHandlerMethod> = java.util.ArrayList(handlersByPriority.size * 2)
            var value = Byte.MIN_VALUE
            do {
                val handlersByListener = handlersByPriority[value]
                if (handlersByListener != null) {
                    for ((key, value1) in handlersByListener) {
                        for (method in value1) {
                            method.isAccessible = true
                            val ehm = SuspendingEventHandlerMethod(coroutineSession, key, method)
                            handlersList.add(ehm)
                        }
                    }
                }
            } while (value++ < Byte.MAX_VALUE)
            byEventBaked.put(eventClass, handlersList.toTypedArray())
        } else {
            byEventBaked.remove(eventClass)
        }
    }

    /**
     * Extension of EventHandler.
     */
    private class SuspendingEventHandlerMethod(
        private val coroutineSession: CoroutineSession,
        lister: Any,
        method: Method
    ) : EventHandlerMethod(lister, method) {

        override fun invoke(event: Any?) {
            val dispatcher = coroutineSession.unconfinedDispatcherBungeeCord

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
    }
}
