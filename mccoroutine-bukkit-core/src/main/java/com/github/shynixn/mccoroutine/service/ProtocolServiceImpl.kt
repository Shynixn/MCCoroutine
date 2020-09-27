@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.mccoroutine.service

import com.github.shynixn.mccoroutine.PlayerPacketEvent
import com.github.shynixn.mccoroutine.contract.ProtocolService
import com.github.shynixn.mccoroutine.findClazz
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Level
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/**
 * Handles packet level manipulation.
 */
internal class ProtocolServiceImpl(private val plugin: Plugin) : ProtocolService {
    private val handlerName = "MCCoroutine " + "-" + UUID.randomUUID().toString()
    private val playerToNmsPlayer = plugin.findClazz("org.bukkit.craftbukkit.VERSION.entity.CraftPlayer")
        .getDeclaredMethod("getHandle")
    private val playerConnectionField = plugin.findClazz("net.minecraft.server.VERSION.EntityPlayer")
        .getDeclaredField("playerConnection")
    private val sendPacketMethod = plugin.findClazz("net.minecraft.server.VERSION.PlayerConnection")
        .getDeclaredMethod("sendPacket", plugin.findClazz("net.minecraft.server.VERSION.Packet"))
    private val dataSerializerClazz = plugin.findClazz("net.minecraft.server.VERSION.PacketDataSerializer")
    private val dataSerializerConstructor = dataSerializerClazz.getDeclaredConstructor(ByteBuf::class.java)
    private val dataSerializationPacketMethod = plugin.findClazz("net.minecraft.server.VERSION.Packet")
        .getDeclaredMethod("b", dataSerializerClazz)
    private val dataDeSerializationPacketMethod = plugin.findClazz("net.minecraft.server.VERSION.Packet")
        .getDeclaredMethod("a", dataSerializerClazz)

    private val cachedPlayerChannels = HashMap<Player, Channel>()
    private val registeredPackets = HashSet<Class<*>>()

    /**
     * Registers the following packets classes for events.
     */
    override fun registerPackets(packets: List<Class<*>>) {
        registeredPackets.addAll(packets)
    }

    /**
     * Registers the player.
     */
    override fun register(player: Player) {
        if (cachedPlayerChannels.containsKey(player)) {
            return
        }

        val nmsPlayer = playerToNmsPlayer
            .invoke(player)
        val connection = playerConnectionField
            .get(nmsPlayer)
        val netWorkManager = plugin.findClazz("net.minecraft.server.VERSION.PlayerConnection")
            .getDeclaredField("networkManager")
            .get(connection)
        val channel = plugin.findClazz("net.minecraft.server.VERSION.NetworkManager")
            .getDeclaredField("channel")
            .get(netWorkManager) as Channel

        val internalInterceptor = PacketInterceptor(player, this)
        channel.pipeline().addBefore("packet_handler", handlerName, internalInterceptor)
        cachedPlayerChannels[player] = channel
    }

    /**
     * Clears the player cache.
     */
    override fun unRegister(player: Player) {
        if (!cachedPlayerChannels.containsKey(player)) {
            return
        }

        val channel = cachedPlayerChannels[player]
        channel!!.eventLoop().execute {
            try {
                channel.pipeline().remove(handlerName)
            } catch (e: Exception) {
                // Can be ignored.
            }
        }
        cachedPlayerChannels.remove(player)
    }

    /**
     * Sends a packet to the given player.
     */
    override fun sendPacket(player: Player, packet: Any) {
        val nmsPlayer = playerToNmsPlayer
            .invoke(player)
        val connection = playerConnectionField
            .get(nmsPlayer)
        sendPacketMethod.invoke(connection, packet)
    }

    /**
     * Sends a byte buffer to the given player.
     */
    override fun sendBytePacket(player: Player, packetClazz: Class<*>, byteBuf: ByteBuf) {
        val packet = packetClazz.newInstance()
        val dataSerializer = dataSerializerConstructor.newInstance(byteBuf)
        dataDeSerializationPacketMethod.invoke(packet, dataSerializer)
        sendPacket(player, packet)
    }

    /**
     * Disposes the protocol service.
     */
    override fun dispose() {
        for (player in cachedPlayerChannels.keys.toTypedArray()) {
            unRegister(player)
        }

        registeredPackets.clear()
    }

    /**
     * On Message receive.
     */
    private fun onMessageReceive(player: Player, packet: Any): Boolean {
        if (cachedPlayerChannels.isEmpty()) {
            return false
        }

        if (!this.registeredPackets.contains(packet.javaClass)) {
            return false
        }

        val buffer = Unpooled.buffer()
        val dataSerializer = dataSerializerConstructor.newInstance(buffer)
        dataSerializationPacketMethod.invoke(packet, dataSerializer)

        plugin.server.scheduler.runTask(plugin, Runnable {
            val packetEvent = PlayerPacketEvent(packet, buffer, player)
            Bukkit.getPluginManager().callEvent(packetEvent)
        })

        return false
    }

    private class PacketInterceptor(
        private val player: Player,
        private val protocolServiceImpl: ProtocolServiceImpl
    ) :
        ChannelDuplexHandler() {
        /**
         * Incoming packet.
         */
        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            val cancelled = try {
                protocolServiceImpl.onMessageReceive(player, msg)
            } catch (e: Exception) {
                Bukkit.getServer().logger.log(Level.SEVERE, "Failed to read packet.", e)
                false
            }

            if (!cancelled) {
                super.channelRead(ctx, msg)
            }
        }

        /**
         * Outgoing packet.
         */
        override fun write(ctx: ChannelHandlerContext?, msg: Any, promise: ChannelPromise?) {
            val cancelled = try {
                protocolServiceImpl.onMessageReceive(player, msg)
            } catch (e: Exception) {
                Bukkit.getServer().logger.log(Level.SEVERE, "Failed to write packet.", e)
                false
            }

            if (!cancelled) {
                super.write(ctx, msg, promise)
            }
        }
    }
}
