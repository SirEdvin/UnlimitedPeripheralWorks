package site.siredvin.peripheralworks.networking

import net.minecraft.network.FriendlyByteBuf

interface NetworkMessage<T> {
    fun type(): MessageType<*>
    fun write(buf: FriendlyByteBuf)
    fun handle(context: T)
}
