package site.siredvin.peripheralworks.fabric

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.networking.MessageType
import site.siredvin.peripheralworks.networking.NetworkMessage

data class FabricMessageType<T : NetworkMessage<*>>(
    val type: CustomPacketPayload.Type<PacketWrapper<T>>,
    val codec: StreamCodec<FriendlyByteBuf, PacketWrapper<T>>,
) : MessageType<T> {

    constructor(id: ResourceLocation, codec: StreamCodec<FriendlyByteBuf, T>) : this(
        CustomPacketPayload.Type(id),
        StreamCodec.of(
            { buf, packet -> codec.encode(buf, packet.payload) },
            { buf -> PacketWrapper(codec.decode(buf)) },
        ),
    )

    companion object {
        @JvmStatic
        fun <T : NetworkMessage<*>> toFabricType(type: MessageType<*>): FabricMessageType<T> {
            @Suppress("UNCHECKED_CAST")
            return type as FabricMessageType<T>
        }

        @JvmStatic
        fun toFabricPacket(message: NetworkMessage<*>): CustomPacketPayload = PacketWrapper(message)
    }

    data class PacketWrapper<T : NetworkMessage<*>>(val payload: T) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = toFabricType<T>(payload.type()).type
    }
}
