package com.github.shynixn.mccoroutine.entity

import com.github.shynixn.mccoroutine.contract.CoroutineSession
import com.github.shynixn.mccoroutine.contract.EventService
import com.github.shynixn.mccoroutine.contract.MCCoroutine
import com.github.shynixn.mccoroutine.invokeSuspend
import com.github.shynixn.mccoroutine.launchMinecraft
import com.github.shynixn.mccoroutine.minecraftDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import org.bukkit.Bukkit
import org.bukkit.Warning
import org.bukkit.event.*
import org.bukkit.plugin.*
import org.bukkit.plugin.java.JavaPluginLoader
import org.spigotmc.CustomTimingsHandler
import java.lang.Deprecated
import java.lang.IllegalArgumentException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import java.util.logging.Level
import kotlin.collections.HashMap
import kotlin.collections.HashSet

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
            val clazz = entry.key as Class<*>
            val handlerList = method.invoke(Bukkit.getPluginManager(), clazz) as HandlerList
            handlerList.registerAll(entry.value as MutableCollection<RegisteredListener>)
        }
    }

    /**
     * Creates a new event flow for the given event clazz.
     */
    override fun <T : Event> createEventFlow(
        event: Class<T>,
        priority: EventPriority,
        ignoredCancelled: Boolean
    ): Flow<T> {
        val executor = EventExecutor { listener, event ->
            coroutineSession.flows[listener]!!.channel.offer(event)
        }
        val listener = object : Listener {}

        for (item in HandlerList.getHandlerLists()) {
            item.register(
                RegisteredListener(
                    listener,
                    executor,
                    EventPriority.NORMAL,
                    plugin,
                    false
                )
            )
        }

        return channelFlow<T> {
            coroutineSession.flows.put(listener, this as ProducerScope<Event>)
            awaitClose {}
        }.flowOn(plugin.minecraftDispatcher)
    }

    private fun createCoroutineListener(listener: Listener, plugin: Plugin): HashMap<*, *> {
        val ret: HashMap<Class<*>, Set<RegisteredListener?>> = HashMap()

        val methods: HashSet<*>
        try {
            val publicMethods = listener.javaClass.methods
            val privateMethods = listener.javaClass.declaredMethods
            methods = HashSet<Any?>(publicMethods.size + privateMethods.size, 1.0f)
            var var11 = publicMethods
            var var10 = publicMethods.size
            var method: Method?
            var var9: Int
            var9 = 0
            while (var9 < var10) {
                method = var11[var9]
                methods.add(method)
                ++var9
            }
            var11 = privateMethods
            var10 = privateMethods.size
            var9 = 0
            while (var9 < var10) {
                method = var11[var9]
                methods.add(method)
                ++var9
            }
        } catch (var15: NoClassDefFoundError) {
            plugin.logger.severe("Plugin " + plugin.description.fullName + " has failed to register events for " + listener.javaClass + " because " + var15.message + " does not exist.")
            return ret
        }

        val var17: Iterator<*> = methods.iterator()

        while (true) {
            while (true) {
                var method: Method
                var eh: EventHandler?
                do {
                    do {
                        do {
                            if (!var17.hasNext()) {
                                return ret
                            }
                            method = var17.next() as Method
                            eh = method.getAnnotation(EventHandler::class.java)
                        } while (eh == null)
                    } while (method.isBridge)
                } while (method.isSynthetic)

                var checkClass: Class<*> = method.getParameterTypes()[0]
                val eventClass = checkClass.asSubclass(Event::class.java)
                method.isAccessible = true

                var eventSet: MutableSet<RegisteredListener?>? = ret[eventClass] as MutableSet<RegisteredListener?>?

                if (eventSet == null) {
                    eventSet = HashSet<RegisteredListener?>()
                    ret[eventClass] = eventSet
                }

                var clazz: Class<*> = eventClass

                while (Event::class.java.isAssignableFrom(clazz)) {
                    if (clazz.getAnnotation(Deprecated::class.java) != null) {
                        val warning = clazz.getAnnotation(Warning::class.java)
                        val warningState: Warning.WarningState = Bukkit.getServer().getWarningState()
                        if (warningState.printFor(warning)) {
                            plugin.logger.log(
                                Level.WARNING,
                                String.format(
                                    "\"%s\" has registered a listener for %s on method \"%s\", but the event is Deprecated. \"%s\"; please notify the authors %s.",
                                    plugin.description.fullName,
                                    clazz.name,
                                    method.toGenericString(),
                                    if (warning != null && warning.reason.length != 0) warning.reason else "Server performance will be affected",
                                    Arrays.toString(plugin.description.authors.toTypedArray())
                                ),
                                if (warningState == Warning.WarningState.ON) AuthorNagException(null as String?) else null
                            )
                        }
                        break
                    }
                    clazz = clazz.superclass
                }

                val timings = CustomTimingsHandler(
                    "Plugin: " + plugin.description.fullName + " Event: " + listener.javaClass.name + "::" + method.name + "(" + eventClass.simpleName + ")",
                    JavaPluginLoader.pluginParentTimer
                )

                val executor = createEventExecutor(plugin, eventClass, method, timings)
                eventSet!!.add(
                    RegisteredListener(
                        listener,
                        executor,
                        eh!!.priority,
                        plugin,
                        eh!!.ignoreCancelled
                    )
                )
            }
        }
    }

    private fun createEventExecutor(
        plugin: Plugin,
        eventClass: Class<*>,
        method: Method,
        timings: CustomTimingsHandler
    ): EventExecutor {
        return EventExecutor { listener, event ->
            try {
                if (eventClass.isAssignableFrom(event.javaClass)) {
                    val isAsync = event.isAsynchronous

                    if (!isAsync) {
                        timings.startTiming()
                    }

                    plugin.launchMinecraft {
                        try {
                            // Try as suspension function.
                            method.invokeSuspend(listener, event)
                        } catch (e: IllegalArgumentException) {
                            // Try as ordinary function.
                            method.invoke(listener, event)
                        }
                    }

                    if (!isAsync) {
                        timings.stopTiming()
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
