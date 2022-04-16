package helper

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import net.md_5.bungee.api.ProxyConfig
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.PluginManager
import net.md_5.bungee.api.scheduler.ScheduledTask
import net.md_5.bungee.api.scheduler.TaskScheduler
import org.mockito.Mockito
import java.util.concurrent.Executors
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext

class MockedBungeeCordServer {
    private val pluginThreadPool = Executors.newFixedThreadPool(4)

    val ionNettyDispatcher: IONettyDispatcher by lazy {
        IONettyDispatcher()
    }

    fun boot(): Plugin {
        val logger = Logger.getAnonymousLogger()
        val proxyServer = Mockito.mock(ProxyServer::class.java)

        val taskScheduler = Mockito.mock(TaskScheduler::class.java)
        Mockito.`when`(
            taskScheduler.runAsync(
                Mockito.any(Plugin::class.java),
                Mockito.any(java.lang.Runnable::class.java)
            )
        ).thenAnswer {
            pluginThreadPool.submit(it.getArgument(1))
            Mockito.mock(ScheduledTask::class.java)
        }

        Mockito.`when`(proxyServer.scheduler).thenReturn(taskScheduler)
        Mockito.`when`(proxyServer.logger).thenReturn(logger)

        val proxyConfig = Mockito.mock(ProxyConfig::class.java)

        val pluginManager = PluginManager(proxyServer)
        Mockito.`when`(proxyServer.config).thenReturn(proxyConfig)

        Mockito.`when`(proxyServer.pluginManager).thenReturn(pluginManager)

        val plugin = Mockito.mock(Plugin::class.java)
        Mockito.`when`(plugin.proxy).thenReturn(proxyServer)
        Mockito.`when`(plugin.executorService).thenReturn(pluginThreadPool)
        Mockito.`when`(plugin.logger).thenReturn(logger)

        val field = ProxyServer::class.java.getDeclaredField("instance")
        field.isAccessible = true
        field.set(null, proxyServer)

        return plugin
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
