package helper

import org.bukkit.Server
import org.bukkit.command.SimpleCommandMap
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginLoader
import org.bukkit.plugin.SimplePluginManager
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask
import org.mockito.Mockito
import java.util.concurrent.Executors
import java.util.logging.Logger

class MockedBukkitServer {
    /**
     * Main Server Thread.
     */
    var mainThreadId: Long = 0L

    /**
     * Boots a new mocked bukkit server with a test plugin.
     */
    fun boot(): Plugin {
        val scheduler = Mockito.mock(BukkitScheduler::class.java)
        val mainThread = Executors.newSingleThreadExecutor()
        mainThread.submit {
            mainThreadId = Thread.currentThread().id
        }
        while (mainThreadId == 0L) {
            Thread.sleep(50)
        }
        val asyncThreadPool = Executors.newFixedThreadPool(4)
        Mockito.`when`(scheduler.runTask(Mockito.any(Plugin::class.java), Mockito.any(Runnable::class.java)))
            .thenAnswer {
                mainThread.submit(it.getArgument(1))
                Mockito.mock(BukkitTask::class.java)
            }
        Mockito.`when`(
            scheduler.runTaskAsynchronously(
                Mockito.any(Plugin::class.java),
                Mockito.any(Runnable::class.java)
            )
        ).thenAnswer {
            asyncThreadPool.submit(it.getArgument(1))
            Mockito.mock(BukkitTask::class.java)
        }
        val server = Mockito.mock(Server::class.java)
        Mockito.`when`(server.scheduler).thenReturn(scheduler)

        val pluginManager = SimplePluginManager(server, Mockito.mock(SimpleCommandMap::class.java))
        Mockito.`when`(server.pluginManager).thenReturn(pluginManager)

        val plugin = Mockito.mock(Plugin::class.java)
        Mockito.`when`(plugin.server).thenReturn(server)
        Mockito.`when`(server.isPrimaryThread).thenAnswer {
            Thread.currentThread().id == mainThreadId
        }
        Mockito.`when`(plugin.isEnabled).thenReturn(true)
        Mockito.`when`(plugin.pluginLoader).thenReturn(Mockito.mock(PluginLoader::class.java))
        Mockito.`when`(plugin.logger).thenReturn(Logger.getAnonymousLogger())

        return plugin
    }
}
