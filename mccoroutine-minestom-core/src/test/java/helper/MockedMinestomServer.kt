package helper

import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minestom.server.MinecraftServer
import net.minestom.server.extensions.Extension

class MockedMinestomServer {
    fun boot(mlogger: ComponentLogger? = null): Extension {
        val extension = MockedExtension(mlogger)
        MinecraftServer.init()
        Thread {
            while (true) {
                MinecraftServer.getSchedulerManager().processTick()
                Thread.sleep(50)
            }
        }.start()
        return extension
    }

    class MockedExtension(private val logger: ComponentLogger?) : Extension() {
        override fun initialize() {
        }

        override fun terminate() {
        }


        /**
         * Gets the logger for the extension
         *
         * @return The logger for the extension
         */
        override fun getLogger(): ComponentLogger {
            if (logger != null) {
                return logger
            }

            return super.getLogger()
        }
    }
}
