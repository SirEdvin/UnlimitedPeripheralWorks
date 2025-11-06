package site.siredvin.peripheralworks.fabric

import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.networking.MessageType
import site.siredvin.peripheralworks.networking.NetworkMessage

data class FabricMessageType<T : NetworkMessage<*>>(
    val type: PacketType<PacketWrapper<T>>
) : MessageType<T> {

    constructor(id: ResourceLocation, reader: FriendlyByteBuf.Reader<T>) : this(
        PacketType.create(id) { b -> PacketWrapper(reader.apply(b)) }
    )

    companion object {
        @JvmStatic
        fun <T : NetworkMessage<*>> toFabricType(type: MessageType<*>): PacketType<PacketWrapper<T>> {
            @Suppress("UNCHECKED_CAST")
            return (type as FabricMessageType<T>).type
        }

        @JvmStatic
        fun toFabricPacket(message: NetworkMessage<*>): FabricPacket {
            return PacketWrapper(message)
        }
    }

    data class PacketWrapper<T : NetworkMessage<*>>(val payload: T) : FabricPacket {
        override fun write(buf: FriendlyByteBuf) {
            payload.write(buf)
        }

        override fun getType(): PacketType<*> {
            return toFabricType<T>(payload.type())
        }
    }
}