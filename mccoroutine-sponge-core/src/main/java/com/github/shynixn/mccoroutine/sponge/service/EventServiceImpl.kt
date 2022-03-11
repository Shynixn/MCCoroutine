package com.github.shynixn.mccoroutine.sponge.service

import com.github.shynixn.mccoroutine.EventExecutionType
import com.github.shynixn.mccoroutine.contract.CoroutineSession
import com.github.shynixn.mccoroutine.contract.EventService
import com.github.shynixn.mccoroutine.launch
import com.github.shynixn.mccoroutine.sponge.extension.invokeSuspend
import com.google.common.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.spongepowered.api.Sponge
import org.spongepowered.api.event.Event
import org.spongepowered.api.event.EventListener
import org.spongepowered.api.event.Listener
import org.spongepowered.api.plugin.PluginContainer
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.reflect.Method
import java.lang.reflect.Type
import java.util.logging.Level
import java.util.logging.Logger

internal class EventServiceImpl(private val plugin: PluginContainer, private val coroutineSession: CoroutineSession) :
    EventService {
    private val logger = Logger.getLogger("MCCoroutine-" + plugin.name)

    /**
     * Registers a suspend listener.
     */
    override fun registerSuspendListener(listener: Any) {
        val spongeEventManagerClazz = Class.forName("org.spongepowered.common.event.SpongeEventManager")
        val registeredListenersField = spongeEventManagerClazz.getDeclaredField("registeredListeners")
        registeredListenersField.isAccessible = true
        val registeredListeners = registeredListenersField.get(Sponge.getEventManager()) as MutableSet<Any>

        if (registeredListeners.contains(listener)) {
            try {
                throw Exception("Stack trace")
            } catch (e: Exception) {
                val writer = StringWriter()
                e.printStackTrace(PrintWriter(writer))
                val data = writer.toString()
                // When using the suspending PluginContainer a false positiv event might be thrown. We can safely ignore that.
                if (!data.contains("com.github.shynixn.mccoroutine.SuspendingPluginContainer.onGameInitializeEvent")) {
                    this.logger.log(
                        Level.SEVERE,
                        "Plugin ${plugin.id} attempted to register an already registered listener ({${listener::class.java.name}})"
                    )

                    Thread.dumpStack()
                    return
                }

                registeredListeners.remove(listener)
            }
        }

        var typeToken = false
        val createRegistrationMethod = try {
            spongeEventManagerClazz.getDeclaredMethod(
                "createRegistration",
                PluginContainer::class.java,
                Type::class.java,
                Listener::class.java,
                EventListener::class.java
            )
        } catch (e: Exception) {
            // Older Sponge version use guava type token.
            typeToken = true
            spongeEventManagerClazz.getDeclaredMethod(
                "createRegistration",
                PluginContainer::class.java,
                TypeToken::class.java,
                Listener::class.java,
                EventListener::class.java
            )
        }

        createRegistrationMethod.isAccessible = true

        val handlers = ArrayList<Any>()

        for (method in listener::class.java.methods) {
            val listenerAnnotation = method.getAnnotation(Listener::class.java) ?: continue

            val eventType: Any = if (!typeToken) {
                method.genericParameterTypes[0]
            } else {
                // Older Sponge version use guava type token.
                TypeToken.of(method.genericParameterTypes[0])
            }

            try {
                // Using the AnnotatedEventListener.Factory will not work because of Filter annotations.
                method.isAccessible = true
                val handler = MCCoroutineEventListener(listener, method, coroutineSession)

                val registration = createRegistrationMethod.invoke(
                    Sponge.getEventManager(),
                    plugin,
                    eventType,
                    listenerAnnotation,
                    handler
                )
                handlers.add(registration)
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to create handler for {${method.name}} on {${listener}}", e)
            }
        }

        registeredListeners.add(listener)
        val registerHandleMethod = spongeEventManagerClazz.getDeclaredMethod("register", List::class.java)
        registerHandleMethod.isAccessible = true
        registerHandleMethod.invoke(Sponge.getEventManager(), handlers)
    }

    /**
     * Fires a suspending [event] with the given [eventExecutionType].
     * @return Collection of receiver jobs. May already be completed.
     */
    override fun fireSuspendingEvent(event: Event, eventExecutionType: EventExecutionType): Collection<Job> {
        val spongeEventManagerClazz = Class.forName("org.spongepowered.common.event.SpongeEventManager")
        val method = spongeEventManagerClazz.getDeclaredMethod("getHandlerCache", Event::class.java)
        method.isAccessible = true
        val listenerCache = method.invoke(Sponge.getEventManager(), event)

        val cacheAccess = Class.forName("org.spongepowered.common.event.RegisteredListener\$Cache")
        val listeners = cacheAccess.getDeclaredMethod("getListeners").invoke(listenerCache) as List<Any>

        val jobs = ArrayList<Job>()

        val listerClazz = Class.forName("org.spongepowered.common.event.RegisteredListener")
        val listenerField = listerClazz.getDeclaredField("listener")
        listenerField.isAccessible = true

        if (eventExecutionType == EventExecutionType.Concurrent) {
            for (listener in listeners) {
                val eventListener = listenerField.get(listener) as EventListener<Event>

                try {
                    if (eventListener is MCCoroutineEventListener) {
                        val job = eventListener.handleSuspend(event)
                        jobs.add(job)
                    } else {
                        eventListener.handle(event)
                    }
                } catch (e: Throwable) {
                    this.logger.log(
                        Level.SEVERE,
                        "Could not pass {${event.javaClass.simpleName}} to {${plugin.name}}.", e
                    )
                }
            }
        } else {
            jobs.add(plugin.launch(Dispatchers.Unconfined) {
                for (listener in listeners) {
                    val eventListener = listenerField.get(listener) as EventListener<Event>

                    try {
                        if (eventListener is MCCoroutineEventListener) {
                            eventListener.handleSuspend(event).join()
                        } else {
                            eventListener.handle(event)
                        }
                    } catch (e: Throwable) {
                        logger.log(
                            Level.SEVERE,
                            "Could not pass {${event.javaClass.simpleName}} to {${plugin.name}}.", e
                        )
                    }
                }
            })
        }

        return jobs
    }

    private class MCCoroutineEventListener(
        private val listener: Any,
        private val method: Method,
        private val coroutineSession: CoroutineSession
    ) :
        EventListener<Event> {

        /**
         * Called when a [Event] registered to this listener is called.
         *
         * @param event The called event
         * @throws Exception If an error occurs
         */
        fun handleSuspend(event: Event): Job {
            return handleEvent(event)
        }

        /**
         * Called when a [Event] registered to this listener is called.
         *
         * @param event The called event
         * @throws Exception If an error occurs
         */
        override fun handle(event: Event) {
            handleEvent(event)
        }

        /**
         * Called when a [Event] registered to this listener is called.
         *
         * @param event The called event
         * @throws Exception If an error occurs
         */
        private fun handleEvent(event: Event): Job {
            val dispatcher = if (!Sponge.getServer().isMainThread) {
                // Unconfined because async events should be supported too.
                Dispatchers.Unconfined
            } else {
                coroutineSession.dispatcherMinecraft
            }

            return coroutineSession.launch(dispatcher) {
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
