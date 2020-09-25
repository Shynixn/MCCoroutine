package com.github.shynixn.mccoroutine

import io.netty.buffer.ByteBuf
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class PlayerPacketEvent(
    /**
     * NMS packet.
     */
    val packet: Any,

    /**
     * Gets the byte data.
     */
    val byteData: ByteBuf,

    /**
     * Associated player receiving or getting sent the packet.
     */
    val player: Player
) : Event(), Cancellable {
    private var isCancelled = false

    /**
     * Event.
     */
    companion object {
        private var handlers = HandlerList()

        /**
         * Handlerlist.
         */
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlers
        }
    }

    /**
     * Returns all handles.
     */
    override fun getHandlers(): HandlerList {
        return PlayerPacketEvent.handlers
    }

    /**
     * Is the event cancelled.
     */
    override fun isCancelled(): Boolean {
        return isCancelled
    }

    /**
     * Sets it cancelled.
     */
    override fun setCancelled(flag: Boolean) {
        this.isCancelled = flag
    }
}
