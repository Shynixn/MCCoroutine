package com.github.shynixn.mccoroutine.bukkit

import org.bukkit.scheduler.BukkitTask
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class CoroutineBukkitTasks : AbstractCoroutineContextElement(CoroutineBukkitTasks) {
    /**
     * Key identifier of the context element.
     */
    companion object Key : CoroutineContext.Key<CoroutineBukkitTasks>

    /**
     * Collection of all bukkit tasks this coroutine has spawned.
     */
    val tasks: Queue<BukkitTask> = ConcurrentLinkedQueue<BukkitTask>()
}
