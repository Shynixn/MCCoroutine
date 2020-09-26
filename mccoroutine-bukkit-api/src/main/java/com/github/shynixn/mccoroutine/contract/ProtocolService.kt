package com.github.shynixn.mccoroutine.contract

import io.netty.buffer.ByteBuf
import org.bukkit.entity.Player

interface ProtocolService {
    /**
     * Registers the following packets classes for events.
     */
    fun registerPackets(packets: List<Class<*>>)

    /**
     * Clears the player cache.
     */
    fun unRegister(player: Player)

    /**
     * Sends a packet to the given player.
     */
    fun sendPacket(player: Player, packet: Any)

    /**
     * Sends a byte buffer to the given player.
     */
    fun sendBytePacket(player: Player, packetClazz: Class<*>, byteBuf: ByteBuf)

    /**
     * Disposes the protocol service.
     */
    fun dispose()

    /**
     * Registers the plaer.
     */
    fun register(player: Player)
}
