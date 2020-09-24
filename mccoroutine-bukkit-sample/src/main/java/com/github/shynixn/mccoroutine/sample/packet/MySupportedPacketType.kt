package com.github.shynixn.mccoroutine.sample.packet

enum class MySupportedPacketType(val simpleName: String, val classPath: String) {
    /**
     * Packet support.
     */
    PACKETPLAYINPOSITIONLOOK(
        "PacketPlayInPositionLook",
        "net.minecraft.server.VERSION.PacketPlayInFlying\$PacketPlayInPositionLook"
    );

    companion object {
        // Cache all key value pair so we can search for them later with O(1).
        private val supportedPacketTypes =
            values().asSequence().map { e -> Pair(e.simpleName, e) }.toMap()

        /**
         * Looks for the supported packet type.
         */
        fun findSupportedPacketType(name: String): MySupportedPacketType? {
            if (supportedPacketTypes.containsKey(name)) {
                return supportedPacketTypes[name]
            }

            return null
        }
    }
}
