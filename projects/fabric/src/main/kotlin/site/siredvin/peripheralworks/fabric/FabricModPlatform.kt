package site.siredvin.peripheralworks.fabric

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModEnvironment
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamDecoder
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ServerGamePacketListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import site.siredvin.broccolium.modules.storage.base.api.SlottedAgnosticStorage
import site.siredvin.broccolium.modules.storage.item.FabricSlottedStorageWrapper
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.api.ISavableComponent
import site.siredvin.peripheralworks.networking.MessageType
import site.siredvin.peripheralworks.networking.NetworkMessage
import site.siredvin.peripheralworks.networking.ServerNetworkContext
import site.siredvin.peripheralworks.xplat.ModInnerPlatform
import site.siredvin.tweakium.modules.platform.FabricInnerComputerBasePlatform

object FabricModPlatform : FabricInnerComputerBasePlatform(), ModInnerPlatform {
    override val modList: List<String>
        get() = FabricLoader.getInstance().allMods.filter { it.metadata.environment != ModEnvironment.SERVER }.map { it.metadata.name }

    override fun getModInformation(mod: String): Map<String, Any>? {
        val mod = FabricLoader.getInstance().allMods.firstOrNull { it.metadata.name == mod && it.metadata.environment != ModEnvironment.SERVER } ?: return null
        return mapOf(
            "name" to mod.metadata.name,
            "version" to mod.metadata.version,
            "description" to mod.metadata.description,
            "license" to mod.metadata.license,
        )
    }

    override fun <T : NetworkMessage<*>> createMessageType(
        id: Int,
        channel: ResourceLocation,
        klass: Class<T>,
        reader: StreamDecoder<FriendlyByteBuf, T>,
    ): MessageType<T> = FabricMessageType(channel, reader)

    @Suppress("UNCHECKED_CAST")
    override fun createServerPacket(message: NetworkMessage<ServerNetworkContext>): Packet<ServerGamePacketListener> {
        val type = FabricMessageType.toFabricType<NetworkMessage<ServerNetworkContext>>(message.type())
        return ClientPlayNetworking.createC2SPacket(type.wrap(message)) as Packet<ServerGamePacketListener>
    }

    override fun createSlottedItemStorage(slots: Int, slotScale: Int, trigger: Runnable): Pair<ISavableComponent, SlottedAgnosticStorage<ItemStack, Int>> {
        val platformStorage = FabricCustomSlottedStorage(slots, slotScale, trigger)
        return Pair(platformStorage, FabricSlottedStorageWrapper(platformStorage))
    }

    override val modID: String
        get() = PeripheralWorksCore.MOD_ID
}
