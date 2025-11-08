package site.siredvin.peripheralworks.networking

import dan200.computercraft.api.network.PacketSender

@JvmRecord
data class Packet(
    val channel: Int,
    val replyChannel: Int,
    val payload: Any,
    val sender: PacketSender,
)
