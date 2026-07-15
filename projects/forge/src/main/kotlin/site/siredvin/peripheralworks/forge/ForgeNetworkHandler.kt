package site.siredvin.peripheralworks.forge

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.codec.StreamDecoder
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ServerGamePacketListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.registration.PayloadRegistrar
import org.apache.logging.log4j.LogManager
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.PeripheralWorksCore.MOD_ID
import site.siredvin.peripheralworks.networking.ClientNetworkContext
import site.siredvin.peripheralworks.networking.MessageType
import site.siredvin.peripheralworks.networking.NetworkMessage
import site.siredvin.peripheralworks.networking.NetworkMessages
import site.siredvin.peripheralworks.networking.ServerNetworkContext

object ForgeNetworkHandler {
    private val logger = LogManager.getLogger("$MOD_ID.networking")

    fun setup(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar(PeripheralWorksCore.NETWORK_VERSION)
        NetworkMessages.serverbound.forEach {
            @Suppress("UNCHECKED_CAST")
            registerServerbound(registrar, it as MessageTypeImpl<NetworkMessage<ServerNetworkContext>>)
        }
        NetworkMessages.clientbound.forEach {
            @Suppress("UNCHECKED_CAST")
            registerClientbound(registrar, it as MessageTypeImpl<NetworkMessage<ClientNetworkContext>>)
        }
    }

    private fun registerServerbound(registrar: PayloadRegistrar, type: MessageTypeImpl<NetworkMessage<ServerNetworkContext>>) {
        registrar.playToServer(type.payloadType, type.codec) { payload, context ->
            handle(payload.message, ServerNetworkContext { context.player() as ServerPlayer })
        }
    }

    private fun registerClientbound(registrar: PayloadRegistrar, type: MessageTypeImpl<NetworkMessage<ClientNetworkContext>>) {
        registrar.playToClient(type.payloadType, type.codec) { payload, _ ->
            handle(payload.message, object : ClientNetworkContext {})
        }
    }

    private fun <H> handle(packet: NetworkMessage<H>, context: H) {
        try {
            packet.handle(context)
        } catch (e: RuntimeException) {
            logger.error("Failed handling packet", e)
            throw e
        } catch (e: Error) {
            logger.error("Failed handling packet", e)
            throw e
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun createClientboundPacket(packet: NetworkMessage<ClientNetworkContext>): Packet<ClientGamePacketListener> {
        val type = packet.type() as MessageTypeImpl<NetworkMessage<ClientNetworkContext>>
        return ClientboundCustomPayloadPacket(type.wrap(packet)) as Packet<ClientGamePacketListener>
    }

    @Suppress("UNCHECKED_CAST")
    fun createServerboundPacket(packet: NetworkMessage<ServerNetworkContext>): Packet<ServerGamePacketListener> {
        val type = packet.type() as MessageTypeImpl<NetworkMessage<ServerNetworkContext>>
        return ServerboundCustomPayloadPacket(type.wrap(packet)) as Packet<ServerGamePacketListener>
    }

    class MessageTypeImpl<T : NetworkMessage<*>>(
        @Suppress("UNUSED_PARAMETER") id: Int,
        val channel: ResourceLocation,
        @Suppress("UNUSED_PARAMETER") klass: Class<T>,
        reader: StreamDecoder<FriendlyByteBuf, T>,
    ) : MessageType<T> {
        val payloadType = CustomPacketPayload.Type<MessagePayload<T>>(channel)
        val codec: StreamCodec<RegistryFriendlyByteBuf, MessagePayload<T>> = object : StreamCodec<RegistryFriendlyByteBuf, MessagePayload<T>> {
            override fun decode(buffer: RegistryFriendlyByteBuf): MessagePayload<T> = wrap(reader.decode(buffer))

            override fun encode(buffer: RegistryFriendlyByteBuf, payload: MessagePayload<T>) {
                payload.message.write(buffer)
            }
        }

        fun wrap(message: T): MessagePayload<T> = MessagePayload(message, payloadType)
    }

    class MessagePayload<T : NetworkMessage<*>>(
        val message: T,
        private val payloadType: CustomPacketPayload.Type<MessagePayload<T>>,
    ) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = payloadType
    }
}
