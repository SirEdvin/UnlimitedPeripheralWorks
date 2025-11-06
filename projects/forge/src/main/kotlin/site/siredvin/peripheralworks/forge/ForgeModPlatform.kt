package site.siredvin.peripheralworks.forge

import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ServerGamePacketListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.fml.ModList
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.registries.DeferredRegister
import site.siredvin.broccolium.modules.storage.energy.Energies
import site.siredvin.broccolium.modules.storage.energy.EnergyUnit
import site.siredvin.peripheralworks.ForgePeripheralWorks
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.networking.MessageType
import site.siredvin.peripheralworks.networking.NetworkMessage
import site.siredvin.peripheralworks.networking.ServerNetworkContext
import site.siredvin.peripheralworks.xplat.ModInnerPlatform
import site.siredvin.tweakium.modules.platform.ForgeInnerComputerBasePlatform
import kotlin.jvm.optionals.getOrNull

object ForgeModPlatform : ForgeInnerComputerBasePlatform(), ModInnerPlatform {
    override val modList: List<String>
        get() = ModList.get().mods.filter { !it.dependencies.any { d -> d.side == IModInfo.DependencySide.SERVER } }.map { it.modId }

    override fun getModInformation(mod: String): Map<String, Any>? {
        val mod = ModList.get().getModContainerById(mod).getOrNull() ?: return null
        if (mod.modInfo.dependencies.any { d -> d.side == IModInfo.DependencySide.SERVER }) {
            return null
        }
        return mapOf(
            "name" to mod.modInfo.modId,
            "description" to mod.modInfo.description,
            "version" to mod.modInfo.version.toString(),
            "license" to mod.modInfo.owningFile.license,
        )
    }

    override fun <T : NetworkMessage<*>> createMessageType(
        id: Int,
        channel: ResourceLocation,
        klass: Class<T>,
        reader: FriendlyByteBuf.Reader<T>
    ): MessageType<T> {
        return NetworkHandler.MessageTypeImpl(id, klass, reader)
    }

    override fun createServerPacket(message: NetworkMessage<ServerNetworkContext>): Packet<ServerGamePacketListener> {
        return NetworkHandler.createServerboundPacket(message)
    }

    override val modID: String
        get() = PeripheralWorksCore.MOD_ID

    override val blocksRegistry: DeferredRegister<Block>
        get() = ForgePeripheralWorks.blocksRegistry

    override val itemsRegistry: DeferredRegister<Item>
        get() = ForgePeripheralWorks.itemsRegistry

    override val blockEntityTypesRegistry: DeferredRegister<BlockEntityType<*>>
        get() = ForgePeripheralWorks.blockEntityTypesRegistry

    override val creativeTabRegistry: DeferredRegister<CreativeModeTab>
        get() = ForgePeripheralWorks.creativeTabRegistry

    override val recipeSerializers: DeferredRegister<RecipeSerializer<*>>
        get() = ForgePeripheralWorks.recipeSerializers

    override val turtleSerializers: DeferredRegister<TurtleUpgradeSerialiser<*>>
        get() = ForgePeripheralWorks.turtleSerializers

    override val pocketSerializers: DeferredRegister<PocketUpgradeSerialiser<*>>
        get() = ForgePeripheralWorks.pocketSerializers
}
