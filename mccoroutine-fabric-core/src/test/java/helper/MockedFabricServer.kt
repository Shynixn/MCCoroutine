package helper

import com.github.shynixn.mccoroutine.fabric.mcCoroutineConfiguration
import net.fabricmc.api.DedicatedServerModInitializer
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class MockedFabricServer {
    fun boot(): DedicatedServerModInitializer {
        val mod = MockedMod()
        mod.onInitializeServer()
        return mod
    }

    class MockedMod : DedicatedServerModInitializer {
        private val threadPool = Executors.newFixedThreadPool(1)

        override fun onInitializeServer() {
            mcCoroutineConfiguration.minecraftExecutor = Executor {
                threadPool.submit(it)
            }
        }
    }
}
