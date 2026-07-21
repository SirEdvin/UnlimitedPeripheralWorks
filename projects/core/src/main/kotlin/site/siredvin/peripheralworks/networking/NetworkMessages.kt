package site.siredvin.peripheralworks.networking

import dan200.computercraft.shared.network.client.ClientNetworkContext
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSet
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.xplat.ModPlatform
import java.util.*

object NetworkMessages {
    private val seenIds: IntSet = IntOpenHashSet()
    private val seenChannel: MutableSet<String> = mutableSetOf()
    private val serverMessages: MutableList<MessageType<out NetworkMessage<ServerNetworkContext>>> = mutableListOf()
    private val clientMessages: MutableList<MessageType<out NetworkMessage<ClientNetworkContext>>> = mutableListOf()

    val GENERIC_EVENT = registerServerbound(
        601,
        "peripheralworks_generic_event",
        MapBasedEventMessage::class.java,
        MapBasedEventMessage.STREAM_CODEC,
    )

    private fun <C, T : NetworkMessage<C>> register(
        messages: MutableList<MessageType<out NetworkMessage<C>>>,
        id: Int,
        channel: String,
        klass: Class<T>,
        codec: StreamCodec<FriendlyByteBuf, T>,
    ): MessageType<T> {
        require(seenIds.add(id)) { "Duplicate id $id" }
        require(seenChannel.add(channel)) { "Duplicate channel $channel" }
        val type = ModPlatform.baseInnerPlatform.createMessageType(id, ResourceLocation.tryBuild(PeripheralWorksCore.MOD_ID, channel)!!, klass, codec)
        messages.add(type)
        return type
    }

    private fun <T : NetworkMessage<ServerNetworkContext>> registerServerbound(
        id: Int,
        channel: String,
        klass: Class<T>,
        codec: StreamCodec<FriendlyByteBuf, T>,
    ): MessageType<T> = register(serverMessages, id, channel, klass, codec)

    private fun <T : NetworkMessage<ClientNetworkContext>> registerClientbound(
        id: Int,
        channel: String,
        klass: Class<T>,
        codec: StreamCodec<FriendlyByteBuf, T>,
    ): MessageType<T> = register(clientMessages, id, channel, klass, codec)

    val serverbound: MutableCollection<MessageType<out NetworkMessage<ServerNetworkContext>>>
        get() = Collections.unmodifiableCollection(serverMessages)

    val clientbound: MutableCollection<MessageType<out NetworkMessage<ClientNetworkContext>>>
        get() = Collections.unmodifiableCollection(clientMessages)

    fun doSomething() {}
}
