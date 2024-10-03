package com.github.shynixn.mccoroutine.folia.test.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

internal class TestMultiThreadsCoroutineDispatcher(private val name : String) :
    CoroutineDispatcher() {
    private val threadPool: ExecutorService = Executors.newFixedThreadPool(4)

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return true
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        threadPool.submit {
            Thread.currentThread().name = name
            block.run()
        }
    }

    fun dispose() {
        threadPool.shutdown()
    }
}
