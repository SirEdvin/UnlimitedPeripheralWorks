package site.siredvin.peripheralworks.fabric

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModEnvironment
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ServerGamePacketListener
import net.minecraft.resources.ResourceLocation
import site.siredvin.broccolium.modules.storage.energy.Energies
import site.siredvin.broccolium.modules.storage.energy.EnergyUnit
import site.siredvin.peripheralworks.PeripheralWorksCore
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
        reader: FriendlyByteBuf.Reader<T>
    ): MessageType<T> {
        return FabricMessageType<T>(channel, reader)
    }

    override fun createServerPacket(message: NetworkMessage<ServerNetworkContext>): Packet<ServerGamePacketListener> {
        val buf = PacketByteBufs.create();
        message.write(buf);
        return ClientPlayNetworking.createC2SPacket(FabricMessageType.toFabricType<NetworkMessage<ServerNetworkContext>>(message.type()).getId(), buf);
    }

    override val modID: String
        get() = PeripheralWorksCore.MOD_ID
}
