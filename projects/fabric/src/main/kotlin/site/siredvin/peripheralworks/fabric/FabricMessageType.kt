package site.siredvin.peripheralworks.fabric

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.codec.StreamDecoder
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.networking.MessageType
import site.siredvin.peripheralworks.networking.NetworkMessage

class FabricMessageType<T : NetworkMessage<*>>(
    channel: ResourceLocation,
    reader: StreamDecoder<FriendlyByteBuf, T>,
) : MessageType<T> {
    val payloadType = CustomPacketPayload.Type<PacketWrapper<T>>(channel)
    val codec: StreamCodec<RegistryFriendlyByteBuf, PacketWrapper<T>> = StreamCodec.of(
        { buffer, packet -> packet.payload.write(buffer) },
        { buffer -> wrap(reader.decode(buffer)) },
    )

    fun wrap(payload: T): PacketWrapper<T> = PacketWrapper(payload, payloadType)

    companion object {
        @JvmStatic
        fun <T : NetworkMessage<*>> toFabricType(type: MessageType<*>): FabricMessageType<T> {
            @Suppress("UNCHECKED_CAST")
            return type as FabricMessageType<T>
        }
    }

    data class PacketWrapper<T : NetworkMessage<*>>(
        val payload: T,
        private val payloadType: CustomPacketPayload.Type<PacketWrapper<T>>,
    ) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = payloadType
    }
}
