package com.github.shynixn.mccoroutine.sample.packet

import io.netty.buffer.ByteBuf

/**
 * Sent by the client to the server when the player moves, looks around.
 */
class MyPacketPlayInPositionLooks(private val byteBuf: ByteBuf) {
    val x: Double
    val y: Double
    val z: Double
    val yaw: Float
    val pitch: Float
    val f: Boolean
    val hasPos: Boolean = true
    val hasLook: Boolean = true

    init {
        x = byteBuf.readDouble()
        y = byteBuf.readDouble()
        z = byteBuf.readDouble()
        yaw = byteBuf.readFloat()
        pitch = byteBuf.readFloat()
        f = byteBuf.readUnsignedByte().toInt() != 0
    }
}
