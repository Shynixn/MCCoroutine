package com.github.shynixn.mccoroutine.bukkit.test.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

internal class TestMinecraftCoroutineDispatcher : CoroutineDispatcher() {
    private val threadPool: ExecutorService = Executors.newFixedThreadPool(1)
    var threadId: Long? = null

    init {
        threadPool.submit {
            Thread.currentThread().name = "[TestMinecraftThread]"
            threadId = Thread.currentThread().id
        }
    }

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return Thread.currentThread().id != threadId
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        threadPool.submit {
            block.run()
        }
    }

    fun dispose() {
        threadPool.shutdown()
    }
}
