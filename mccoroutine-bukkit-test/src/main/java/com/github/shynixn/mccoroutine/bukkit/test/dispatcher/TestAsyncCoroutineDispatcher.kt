package com.github.shynixn.mccoroutine.bukkit.test.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

internal class TestAsyncCoroutineDispatcher(private val minecraftDispatcher: TestMinecraftCoroutineDispatcher) :
    CoroutineDispatcher() {
    private val threadPool: ExecutorService = Executors.newFixedThreadPool(4)

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return Thread.currentThread().id == minecraftDispatcher.threadId
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        threadPool.submit {
            Thread.currentThread().name = "[TestAsyncThread]"
            block.run()
        }
    }

    fun dispose() {
        threadPool.shutdown()
    }
}
