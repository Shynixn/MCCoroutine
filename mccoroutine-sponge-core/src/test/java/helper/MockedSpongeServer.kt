package helper

import com.google.common.collect.ListMultimap
import org.mockito.Mockito
import org.spongepowered.api.Game
import org.spongepowered.api.GameRegistry
import org.spongepowered.api.Server
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandMapping
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.plugin.PluginManager
import org.spongepowered.api.scheduler.Task
import org.spongepowered.api.util.ResettableBuilder
import org.spongepowered.common.SpongeImpl
import org.spongepowered.common.command.SpongeCommandDisambiguator
import org.spongepowered.common.command.SpongeCommandManager
import org.spongepowered.common.config.SpongeConfig
import org.spongepowered.common.event.SpongeEventManager
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.BiConsumer
import java.util.function.Consumer

class MockedSpongeServer {
    private val executorMain = Executors.newSingleThreadExecutor()
    private val executorAsync = Executors.newFixedThreadPool(4)
    var mainThreadId = 0L

    init {
        executorMain.submit {
            mainThreadId = Thread.currentThread().id
        }
    }

    fun callCommand(player: Player, command : String){
        val dispatcherField = SpongeCommandManager::class.java.getDeclaredField("dispatcher")
        dispatcherField.isAccessible = true
        val dispatcher = dispatcherField.get(Sponge.getCommandManager())
        val fieldListCommand = dispatcher.javaClass.getDeclaredField("commands")
        fieldListCommand.isAccessible = true
        val mappings = fieldListCommand.get(dispatcher) as ListMultimap<String, CommandMapping>
        mappings[command]!![0].callable.process(player, "")
    }
    /**
     * Boots a new mocked sponge server with a test plugin.
     */
    fun boot(): PluginContainer {
        val gameRegistry = Mockito.mock(GameRegistry::class.java)
        val pluginManager = Mockito.mock(PluginManager::class.java)
        val eventManager = SpongeEventManager(Mockito.mock(org.apache.logging.log4j.Logger::class.java), pluginManager)
        val game = Mockito.mock(Game::class.java)

        val registry = Sponge::class.java.getDeclaredField("registry")
        registry.isAccessible = true
        registry.set(null, gameRegistry)
        val eventManagerField = Sponge::class.java.getDeclaredField("eventManager")
        eventManagerField.isAccessible = true
        eventManagerField.set(null, eventManager)
        val gameField = Sponge::class.java.getDeclaredField("game")
        gameField.isAccessible = true
        gameField.set(null, game)
        val server = Mockito.mock(Server::class.java)
        Mockito.`when`(game.server).thenReturn(server)
        Mockito.`when`(server.isMainThread).thenAnswer {
            Thread.currentThread().id == mainThreadId
        }
        val plugin = Mockito.mock(PluginContainer::class.java)
        Mockito.`when`(gameRegistry.createBuilder<Task.Builder>(Mockito.any())).thenAnswer {
            TaskBuilderMock(this)
        }

        Mockito.`when`(game.pluginManager).thenReturn(pluginManager)
        val commandManager = SpongeCommandManager(Mockito.mock(org.apache.logging.log4j.Logger::class.java))
        val commandManagerField = Sponge::class.java.getDeclaredField("commandManager")
        commandManagerField.isAccessible = true
        commandManagerField.set(null, commandManager)

        Mockito.`when`(pluginManager.fromInstance(Mockito.any())).thenReturn(Optional.of(plugin))


        var spongeConfigField = SpongeImpl::class.java.getDeclaredField("customDataConfigAdapter")
        spongeConfigField.isAccessible = true
        spongeConfigField.set(null, Mockito.mock(SpongeConfig::class.java))

        spongeConfigField = SpongeImpl::class.java.getDeclaredField("globalConfigAdapter")
        spongeConfigField.isAccessible = true
        spongeConfigField.set(null, Mockito.mock(SpongeConfig::class.java))

        return plugin
    }

    private class TaskBuilderMock(private val server: MockedSpongeServer) : Task.Builder {
        private var isAsync = false
        private var runnAble: Runnable? = null

        override fun from(value: Task): Task.Builder {
            TODO("Not yet implemented")
        }

        override fun reset(): Task.Builder {
            TODO("Not yet implemented")
        }

        override fun async(): Task.Builder {
            isAsync = true
            return this
        }

        override fun execute(executor: Consumer<Task>): Task.Builder {
            runnAble = Runnable {
                executor.accept(Mockito.mock(Task::class.java))
            }
            return this
        }

        override fun delay(delay: Long, unit: TimeUnit): Task.Builder {
            TODO("Not yet implemented")
        }

        override fun delayTicks(ticks: Long): Task.Builder {
            TODO("Not yet implemented")
        }

        override fun interval(interval: Long, unit: TimeUnit): Task.Builder {
            TODO("Not yet implemented")
        }

        override fun intervalTicks(ticks: Long): Task.Builder {
            TODO("Not yet implemented")
        }

        override fun name(name: String): Task.Builder {
            TODO("Not yet implemented")
        }

        override fun submit(plugin: Any): Task {
            if (isAsync) {
                server.executorAsync.submit(runnAble!!)
            } else {
                server.executorMain.submit(runnAble!!)
            }
            return Mockito.mock(Task::class.java)
        }
    }
}
