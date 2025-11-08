package site.siredvin.peripheralworks.xplat

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ServerGamePacketListener
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.networking.MessageType
import site.siredvin.peripheralworks.networking.NetworkMessage
import site.siredvin.peripheralworks.networking.ServerNetworkContext
import site.siredvin.tweakium.modules.platform.api.InnerComputerBasePlatform

interface ModInnerPlatform : InnerComputerBasePlatform {
    val modList: List<String>
    fun getModInformation(mod: String): Map<String, Any>?

    fun <T : NetworkMessage<*>> createMessageType(
        id: Int,
        channel: ResourceLocation,
        klass: Class<T>,
        reader: FriendlyByteBuf.Reader<T>,
    ): MessageType<T>

    // TODO: when move it to libs, split it into client code. I need to have client code at least sometimes
    fun createServerPacket(message: NetworkMessage<ServerNetworkContext>): Packet<ServerGamePacketListener>
}
