package helper

import net.minestom.server.MinecraftServer
import net.minestom.server.extensions.Extension
import org.slf4j.Logger

class MockedMinestomServer {
    fun boot(mlogger: Logger? = null): Extension {
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

    class MockedExtension(private val logger: Logger?) : Extension() {
        override fun initialize() {
        }

        override fun terminate() {
        }


        /**
         * Gets the logger for the extension
         *
         * @return The logger for the extension
         */
        override fun getLogger(): Logger {
            if (logger != null) {
                return logger
            }

            return super.getLogger()
        }
    }
}
