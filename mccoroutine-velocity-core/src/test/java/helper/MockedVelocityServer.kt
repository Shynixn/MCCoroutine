package helper

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.PluginDescription
import com.velocitypowered.api.plugin.PluginManager
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.scheduler.ScheduledTask
import com.velocitypowered.api.scheduler.Scheduler
import com.velocitypowered.proxy.command.VelocityCommandManager
import com.velocitypowered.proxy.event.VelocityEventManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import org.mockito.Mockito
import org.slf4j.Logger
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class MockedVelocityServer {
    private val pluginThreadPool = Executors.newFixedThreadPool(4)

    val ionNettyDispatcher: IONettyDispatcher by lazy {
        IONettyDispatcher()
    }

    lateinit var proxyServer: ProxyServer

    fun boot(mlogger: Logger? = null): PluginContainer {
        val logger = if (mlogger == null) {
            Mockito.mock(org.slf4j.Logger::class.java)
        } else {
            mlogger
        }
        val server = Mockito.mock(ProxyServer::class.java)
        val pluginManager = Mockito.mock(PluginManager::class.java)
        val eventManager = VelocityEventManager(pluginManager)

        Mockito.`when`(server.eventManager).thenReturn(eventManager)
        val scheduler = Mockito.mock(Scheduler::class.java)
        Mockito.`when`(scheduler.buildTask(Mockito.any(), Mockito.any(java.lang.Runnable::class.java))).thenAnswer {
            val runnable = it.getArgument<Runnable>(1)
            object : Scheduler.TaskBuilder {
                override fun delay(time: Long, unit: TimeUnit?): Scheduler.TaskBuilder {
                    return this
                }

                override fun repeat(time: Long, unit: TimeUnit?): Scheduler.TaskBuilder {
                    return this
                }

                override fun clearDelay(): Scheduler.TaskBuilder {
                    return this
                }

                override fun clearRepeat(): Scheduler.TaskBuilder {
                    return this
                }

                override fun schedule(): ScheduledTask {
                    pluginThreadPool.submit(runnable)
                    return Mockito.mock(ScheduledTask::class.java)
                }
            }
        }
        Mockito.`when`(server.scheduler).thenReturn(scheduler)
        Mockito.`when`(server.pluginManager).thenReturn(pluginManager)
        Mockito.`when`(pluginManager.isLoaded(Mockito.anyString())).thenReturn(true)

        val commandManager = VelocityCommandManager(eventManager)
        Mockito.`when`(server.commandManager).thenReturn(commandManager)

        val plugin = MockedPluginContainer(server, logger)
        proxyServer = server

        val suspendingPluginContainer = SuspendingPluginContainer(plugin, server, logger)
        suspendingPluginContainer.initialize(plugin.instance.get())

        return plugin
    }

    class MockedPlugin {
    }

    class MockedPluginContainer(private val proxyServer: ProxyServer, private val logger: org.slf4j.Logger) :
        PluginContainer {
        private val mockedPlugin = MockedPlugin()

        /**
         * Returns the plugin's description.
         *
         * @return the plugin's description
         */
        override fun getDescription(): PluginDescription {
            val description = Mockito.mock(PluginDescription::class.java)
            Mockito.`when`(description.id).thenReturn("test")
            return description
        }

        override fun getInstance(): Optional<*> {
            return Optional.of(this)
        }
    }

    /**
     * Mocks the netty dispatcher.
     */
    class IONettyDispatcher : CoroutineDispatcher() {
        val executorService = Executors.newSingleThreadExecutor()
        var threadId = 0L

        init {
            executorService.submit {
                threadId = Thread.currentThread().id
            }
        }

        /**
         * Dispatches execution of a runnable [block] onto another thread in the given [context].
         * This method should guarantee that the given [block] will be eventually invoked,
         * otherwise the system may reach a deadlock state and never leave it.
         * Cancellation mechanism is transparent for [CoroutineDispatcher] and is managed by [block] internals.
         *
         * This method should generally be exception-safe. An exception thrown from this method
         * may leave the coroutines that use this dispatcher in the inconsistent and hard to debug state.
         *
         * This method must not immediately call [block]. Doing so would result in [StackOverflowError]
         * when [yield] is repeatedly called from a loop. However, an implementation that returns `false` from
         * [isDispatchNeeded] can delegate this function to `dispatch` method of [Dispatchers.Unconfined], which is
         * integrated with [yield] to avoid this problem.
         */
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            executorService.submit(block)
        }
    }
}
