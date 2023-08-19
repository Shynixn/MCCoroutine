package com.github.shynixn.mccoroutine.folia.service

import com.github.shynixn.mccoroutine.folia.CoroutineSession
import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import com.github.shynixn.mccoroutine.folia.extension.invokeSuspend
import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.bukkit.Warning
import org.bukkit.event.*
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.*
import java.lang.Deprecated
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.logging.Level
import kotlin.Boolean
import kotlin.IllegalArgumentException
import kotlin.String
import kotlin.Throwable
import kotlin.check
import kotlin.coroutines.CoroutineContext

internal class EventServiceImpl(private val plugin: Plugin, private val coroutineSession: CoroutineSession) {
    /**
     * Registers a suspend listener.
     */
    fun registerSuspendListener(
        listener: Listener,
        eventDispatcher: Map<Class<out Event>, (event: Event) -> CoroutineContext>
    ) {
        val registeredListeners = createCoroutineListener(listener, plugin, eventDispatcher)

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
     * Fires a suspending [event] with the given [eventExecutionType].
     * @return Collection of receiver jobs. May already be completed.
     */
    fun fireSuspendingEvent(
        event: Event,
        eventExecutionType: com.github.shynixn.mccoroutine.folia.EventExecutionType
    ): Collection<Job> {
        if (event.isAsynchronous) {
            check(!Thread.holdsLock(this)) { event.eventName + " cannot be triggered asynchronously from inside synchronized code." }
            check(!plugin.server.isPrimaryThread) { event.eventName + " cannot be triggered asynchronously from primary server thread." }
        } else {
            check(plugin.server.isPrimaryThread) { event.eventName + " cannot be triggered asynchronously from another thread." }
        }

        val listeners = event.handlers.registeredListeners
        val jobs = ArrayList<Job>()

        if (eventExecutionType == com.github.shynixn.mccoroutine.folia.EventExecutionType.Concurrent) {
            for (registration in listeners) {
                if (!registration.plugin.isEnabled) {
                    continue
                }

                try {
                    if (registration is SuspendingRegisteredListener) {
                        val job = registration.callSuspendingEvent(event)
                        jobs.add(job)
                    } else {
                        registration.callEvent(event)
                    }
                } catch (e: Throwable) {
                    plugin.logger.log(
                        Level.SEVERE,
                        "Could not pass event " + event.eventName + " to " + registration.plugin.description.fullName, e
                    )
                }
            }
        } else if (eventExecutionType == com.github.shynixn.mccoroutine.folia.EventExecutionType.Consecutive) {
            jobs.add(plugin.launch(Dispatchers.Unconfined, CoroutineStart.UNDISPATCHED) {
                for (registration in listeners) {
                    if (!registration.plugin.isEnabled) {
                        continue
                    }
                    try {
                        if (registration is SuspendingRegisteredListener) {
                            registration.callSuspendingEvent(event).join()
                        } else {
                            registration.callEvent(event)
                        }
                    } catch (e: Throwable) {
                        plugin.logger.log(
                            Level.SEVERE,
                            "Could not pass event " + event.eventName + " to " + registration.plugin.description.fullName,
                            e
                        )
                    }
                }
            })
        }

        return jobs
    }


    /**
     * Creates a listener according to the spigot implementation.
     */
    private fun createCoroutineListener(
        listener: Listener,
        plugin: Plugin,
        eventDispatcher: Map<Class<out Event>, (event: Event) -> CoroutineContext>
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

            if (!eventDispatcher.containsKey(eventClass)) {
                throw IllegalArgumentException("A event dispatcher for class '" + eventClass.name + "' needs to be added to registerSuspendingEvents.")
            }

            val contextResolver = eventDispatcher[eventClass]!!

            val executor = SuspendingEventExecutor(eventClass, method, plugin, coroutineSession, contextResolver)
            result[eventClass]!!.add(
                SuspendingRegisteredListener(
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

    class SuspendingEventExecutor(
        private val eventClass: Class<*>,
        private val method: Method,
        private val plugin: Plugin,
        private val coroutineSession: CoroutineSession,
        private val contextResolver: (event: Event) -> CoroutineContext
    ) : EventExecutor {
        var isSuspendMethod: Boolean? = null
        fun executeSuspend(listener: Listener, event: Event): Job {
            return executeEvent(listener, event)
        }

        override fun execute(listener: Listener, event: Event) {
            executeEvent(listener, event)
        }

        private fun executeEvent(listener: Listener, event: Event): Job {
            try {
                if (eventClass.isAssignableFrom(event.javaClass)) {
                    val isAsync = event.isAsynchronous

                    val dispatcher = if (isAsync) {
                        plugin.asyncDispatcher
                    } else {
                        if (coroutineSession.isFoliaLoaded) {
                            contextResolver.invoke(event)
                        } else {
                            plugin.globalRegionDispatcher // Load minecraft dispatcher
                        }
                    }

                    // We want to start it on the same thread as the calling thread -> unDispatched.
                    // However, after a possible suspension we either end up on the asyncDispatcher or minecraft Dispatcher.
                    return plugin.launch(dispatcher, CoroutineStart.UNDISPATCHED) {
                        if (isSuspendMethod == null) {
                            try {
                                // Try as suspension function.
                                method.invokeSuspend(listener, event)
                                isSuspendMethod = true
                            } catch (e: IllegalArgumentException) {
                                // Try as ordinary function.
                                method.invoke(listener, event)
                                isSuspendMethod = false
                            }
                        } else if (isSuspendMethod!!) {
                            method.invokeSuspend(listener, event)
                        } else {
                            method.invoke(listener, event)
                        }
                    }
                }
            } catch (var4: InvocationTargetException) {
                throw EventException(var4.cause)
            } catch (var5: Throwable) {
                throw EventException(var5)
            }
            return Job()
        }
    }

    class SuspendingRegisteredListener(
        lister: Listener,
        private val executorImpl: EventExecutor,
        priority: EventPriority,
        plugin: Plugin,
        ignoreCancelled: Boolean
    ) : RegisteredListener(lister, executorImpl, priority, plugin, ignoreCancelled) {
        fun callSuspendingEvent(event: Event): Job {
            if (event is Cancellable) {
                if ((event as Cancellable).isCancelled && isIgnoringCancelled) {
                    return Job()
                }
            }

            return if (executorImpl is SuspendingEventExecutor) {
                executorImpl.executeSuspend(listener, event)
            } else {
                executorImpl.execute(listener, event)
                Job()
            }
        }
    }
}
