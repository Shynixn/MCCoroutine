package helper

import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.SimpleCommandMap
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.SimplePluginManager
import org.bukkit.plugin.java.JavaPluginLoader
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask
import org.mockito.Mockito
import java.util.concurrent.Executors
import java.util.logging.Logger

class MockedBukkitServer {
    companion object {
        private val asyncThreadPool = Executors.newFixedThreadPool(4)
        private val mainThread = Executors.newSingleThreadExecutor()
        private var plugin: Plugin? = null
        private var mainThreadIdHandle: Long = 0L
    }

    /**
     * Main Server Thread.
     */
    val mainThreadId: Long
        get() {
            return mainThreadIdHandle
        }

    /**
     * Boots a new mocked bukkit server with a test plugin.
     */
    fun boot(): Plugin {
        if (plugin != null) {
            return plugin!!
        }

        val scheduler = Mockito.mock(BukkitScheduler::class.java)

        mainThread.submit {
            mainThreadIdHandle = Thread.currentThread().id
        }
        while (mainThreadId == 0L) {
            Thread.sleep(50)
        }

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

        plugin = Mockito.mock(Plugin::class.java)
        Mockito.`when`(plugin!!.server).thenReturn(server)
        Mockito.`when`(server.isPrimaryThread).thenAnswer {
            val id = Thread.currentThread().id

            id == mainThreadId
        }
        val loader = JavaPluginLoader(server)

        Mockito.`when`(plugin!!.isEnabled).thenReturn(true)
        Mockito.`when`(plugin!!.pluginLoader).thenReturn(loader)
        Mockito.`when`(plugin!!.logger).thenReturn(Logger.getAnonymousLogger())
        val pluginDescription = Mockito.mock(PluginDescriptionFile::class.java)
        Mockito.`when`(plugin!!.description).thenReturn(pluginDescription)

        val serverField = Bukkit::class.java.getDeclaredField("server")
        serverField.isAccessible = true
        serverField.set(null, server)

        return plugin!!
    }
}
