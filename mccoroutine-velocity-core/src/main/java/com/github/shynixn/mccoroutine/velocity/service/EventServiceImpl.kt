package com.github.shynixn.mccoroutine.velocity.service

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.github.shynixn.mccoroutine.velocity.extension.invokeSuspend
import com.github.shynixn.mccoroutine.velocity.launch
import com.github.shynixn.mccoroutine.velocity.velocityDispatcher
import com.velocitypowered.api.event.EventHandler
import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.plugin.PluginContainer
import kotlinx.coroutines.CoroutineStart
import java.lang.reflect.Method
import kotlin.coroutines.Continuation

class EventServiceImpl(
    private val pluginContainer: PluginContainer,
    private val suspendingPluginContainer: SuspendingPluginContainer
) {
    /**
     * Registers the given listener.
     */
    fun registerListener(listener: Any, onlyRegisterSuspend : Boolean) {
        if (!onlyRegisterSuspend) {
            require(pluginContainer != listener) { "The plugin main instance is automatically registered." }
        }

        registerInternally(listener, onlyRegisterSuspend)
    }

    private fun registerInternally(listener: Any, onlyRegisterSuspend: Boolean) {
        val eventManager = suspendingPluginContainer.server.eventManager

        val targetClass: Class<*> = listener.javaClass
        val collected = HashMap<String?, Any?>()

        val eventManagerClass = Class.forName("com.velocitypowered.proxy.event.VelocityEventManager")
        val collectMethodsMethod =
            eventManagerClass.getDeclaredMethod("collectMethods", Class::class.java, Map::class.java)
        collectMethodsMethod.isAccessible = true
        collectMethodsMethod.invoke(eventManager, targetClass, collected)

        val registrations: MutableList<Any> = ArrayList()
        val var6: Iterator<*> = collected.values.iterator()
        val methodHandlerInfoClass =
            Class.forName("com.velocitypowered.proxy.event.VelocityEventManager\$MethodHandlerInfo")
        val methodHandlerInfoErrorField = methodHandlerInfoClass.getDeclaredField("errors")
        methodHandlerInfoErrorField.isAccessible = true
        val methodHandlerInfoMethodField = methodHandlerInfoClass.getDeclaredField("method")
        methodHandlerInfoMethodField.isAccessible = true
        val methodHandlerContinuationField = methodHandlerInfoClass.getDeclaredField("continuationType")
        methodHandlerContinuationField.isAccessible = true
        val methodHandlerEventTypeField = methodHandlerInfoClass.getDeclaredField("eventType")
        methodHandlerEventTypeField.isAccessible = true
        val methodHandlerorderField = methodHandlerInfoClass.getDeclaredField("order")
        methodHandlerorderField.isAccessible = true

        val loadingCacheClass = Class.forName("com.github.benmanes.caffeine.cache.LoadingCache")
        val loadingCacheClassGetMethod = loadingCacheClass.getDeclaredMethod("get", Any::class.java)

        val untargetedMethodHandlerField = eventManagerClass.getDeclaredField("untargetedMethodHandlers")
        untargetedMethodHandlerField.isAccessible = true
        val unTargetedMethodHandlers = untargetedMethodHandlerField.get(eventManager)

        val untargetedEventHandlerClass = Class.forName("com.velocitypowered.proxy.event.UntargetedEventHandler")
        val buildHandlerMethod = untargetedEventHandlerClass.getDeclaredMethod("buildHandler", Any::class.java)

        val handlerRegistrationClass =
            Class.forName("com.velocitypowered.proxy.event.VelocityEventManager\$HandlerRegistration")
        val handlerRegistrationClassConstructor = handlerRegistrationClass.getDeclaredConstructor(
            PluginContainer::class.java,
            Short::class.java,
            Class::class.java,
            Any::class.java,
            EventHandler::class.java
        )
        handlerRegistrationClassConstructor.isAccessible = true

        while (var6.hasNext()) {
            val info = var6.next()
            val errors = methodHandlerInfoErrorField.get(info)
            val method = methodHandlerInfoMethodField.get(info) as Method
            val continuationType = methodHandlerContinuationField.get(info) as Class<*>?
            val eventType = methodHandlerEventTypeField.get(info) as Class<*>
            val order = methodHandlerorderField.get(info) as Short

            if (continuationType == Continuation::class.java) {
                val handler = object : EventHandler<Any> {
                    override fun execute(event: Any) {
                        // Start unDispatched on the same thread but end up on the velocity dispatcher.
                        pluginContainer.launch(pluginContainer.velocityDispatcher, CoroutineStart.UNDISPATCHED) {
                            method.invokeSuspend(listener, event)
                        }
                    }

                    override fun executeAsync(event: Any): EventTask? {
                        return EventTask.withContinuation { continuation: com.velocitypowered.api.event.Continuation ->
                            // Start unDispatched on the same thread but end up on the velocity dispatcher.
                            pluginContainer.launch(pluginContainer.velocityDispatcher, CoroutineStart.UNDISPATCHED) {
                                method.invokeSuspend(listener, event)
                                continuation.resume()
                            }
                        }
                    }
                }

                val handlerRegistration = handlerRegistrationClassConstructor.newInstance(
                    pluginContainer,
                    order,
                    eventType,
                    listener,
                    handler
                )
                registrations.add(handlerRegistration)
                continue
            }

            if (errors != null) {
                suspendingPluginContainer.logger.info(
                    "Invalid listener method {} in {}: {}",
                    method.getName(),
                    method.getDeclaringClass().getName(),
                    errors
                )
                continue
            }

            val untargetedHandler = loadingCacheClassGetMethod.invoke(unTargetedMethodHandlers, method)
            val handler = buildHandlerMethod.invoke(untargetedHandler, listener) as EventHandler<Any>
            val handlerRegistration =
                handlerRegistrationClassConstructor.newInstance(pluginContainer, order, eventType, listener, handler)

            if (!onlyRegisterSuspend) {
                registrations.add(handlerRegistration)
            }
        }

        val registrationMethod = eventManagerClass.getDeclaredMethod("register", List::class.java)
        registrationMethod.isAccessible = true
        registrationMethod.invoke(eventManager, registrations)
    }
}
