package helper

import org.mockito.Mockito
import org.spongepowered.api.Game
import org.spongepowered.api.GameRegistry
import org.spongepowered.api.Server
import org.spongepowered.api.Sponge
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.plugin.PluginManager
import org.spongepowered.common.event.SpongeEventManager

class MockedSpongeServer {

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
        Mockito.`when`(server.isMainThread).thenReturn(false)
        val plugin = Mockito.mock(PluginContainer::class.java)

        return plugin
    }

}
