package site.siredvin.peripheralworks.forge

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ServerGamePacketListener
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.simple.SimpleChannel
import org.apache.logging.log4j.LogManager
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.PeripheralWorksCore.MOD_ID
import site.siredvin.peripheralworks.networking.ClientNetworkContext
import site.siredvin.peripheralworks.networking.MessageType
import site.siredvin.peripheralworks.networking.NetworkMessage
import site.siredvin.peripheralworks.networking.NetworkMessages
import site.siredvin.peripheralworks.networking.ServerNetworkContext
import java.util.function.BiConsumer
import java.util.function.Predicate
import java.util.function.Supplier


object NetworkHandler {
    var logger = LogManager.getLogger("$MOD_ID.networking")
    private val network: SimpleChannel

    init {
        val version = PeripheralWorksCore.NETWORK_VERSION
        network = NetworkRegistry.ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(MOD_ID, "network"))
            .networkProtocolVersion(Supplier { version })
            .clientAcceptedVersions(Predicate { anObject: String -> version == anObject })
            .serverAcceptedVersions(Predicate { anObject: String -> version == anObject })
            .simpleChannel()
    }

    fun setup() {
        for (type in NetworkMessages.serverbound) {
            val forgeType = type as MessageTypeImpl<out NetworkMessage<ServerNetworkContext>>
            registerMainThread(
                forgeType,
                NetworkDirection.PLAY_TO_SERVER,
                { c -> ServerNetworkContext { c.sender!! } })
        }

        for (type in NetworkMessages.clientbound) {
            @Suppress("UNCHECKED_CAST") val forgeType = type as MessageTypeImpl<out NetworkMessage<ClientNetworkContext>>
            registerMainThread(
                forgeType,
                NetworkDirection.PLAY_TO_CLIENT,
                { x -> object: ClientNetworkContext {} })
        }
    }

    fun createClientboundPacket(packet: NetworkMessage<ClientNetworkContext>): Packet<ClientGamePacketListener> {
        @Suppress("UNCHECKED_CAST")
        return network.toVanillaPacket<Any?>(
            packet,
            NetworkDirection.PLAY_TO_CLIENT
        ) as Packet<ClientGamePacketListener>
    }

    fun createServerboundPacket(packet: NetworkMessage<ServerNetworkContext>): Packet<ServerGamePacketListener> {
        @Suppress("UNCHECKED_CAST")
        return network.toVanillaPacket<Any>(
            packet,
            NetworkDirection.PLAY_TO_SERVER
        ) as Packet<ServerGamePacketListener>
    }

    fun <H, T : NetworkMessage<H>> registerMainThread(
        type: MessageTypeImpl<T>, direction: NetworkDirection, handler: (NetworkEvent.Context) -> H
    ) {
        network.messageBuilder<T?>(type.klass, type.id, direction)
            .encoder(NetworkMessage<H>::write)
            .decoder(type.reader)
            .consumerMainThread { packet: T, contextSup: Supplier<NetworkEvent.Context> ->
                try {
                    packet.handle(handler(contextSup.get()))
                } catch (e: RuntimeException) {
                    logger.error("Failed handling packet", e)
                    throw e
                } catch (e: Error) {
                    logger.error("Failed handling packet", e)
                    throw e
                }
            }
            .add()
    }

    class MessageTypeImpl<T : NetworkMessage<*>>(
        val id: Int, val klass: Class<T>, val reader:  FriendlyByteBuf.Reader<T>
    ) : MessageType<T>
}