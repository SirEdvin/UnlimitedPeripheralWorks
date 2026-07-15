package site.siredvin.peripheralworks.forge

import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.upgrades.UpgradeType
import net.minecraft.advancements.CriterionTrigger
import net.minecraft.core.component.DataComponentType
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamDecoder
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ServerGamePacketListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.fml.ModList
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforgespi.language.IModInfo
import site.siredvin.broccolium.modules.storage.base.api.SlottedAgnosticStorage
import site.siredvin.broccolium.modules.storage.item.AgnosticItemHandlerWrapper
import site.siredvin.peripheralworks.ForgePeripheralWorks
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.api.ISavableComponent
import site.siredvin.peripheralworks.networking.MessageType
import site.siredvin.peripheralworks.networking.NetworkMessage
import site.siredvin.peripheralworks.networking.ServerNetworkContext
import site.siredvin.peripheralworks.xplat.ModInnerPlatform
import site.siredvin.tweakium.modules.platform.ForgeInnerComputerBasePlatform
import java.util.function.Supplier
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
        reader: StreamDecoder<FriendlyByteBuf, T>,
    ): MessageType<T> = ForgeNetworkHandler.MessageTypeImpl(id, channel, klass, reader)

    override fun createServerPacket(message: NetworkMessage<ServerNetworkContext>): Packet<ServerGamePacketListener> = ForgeNetworkHandler.createServerboundPacket(message)

    override fun createSlottedItemStorage(
        slots: Int,
        slotScale: Int,
        trigger: Runnable,
    ): Pair<ISavableComponent, SlottedAgnosticStorage<ItemStack, Int>> {
        val platformStorage = ForgeCustomSlottedStorage(slots, slotScale, trigger)
        return Pair(platformStorage, AgnosticItemHandlerWrapper(platformStorage))
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

    override val criterionTriggers: DeferredRegister<CriterionTrigger<*>>
        get() = ForgePeripheralWorks.criterionTriggers

    override val dataComponentTypesRegistry: DeferredRegister<DataComponentType<*>>
        get() = ForgePeripheralWorks.dataComponentTypes

    override fun <V : ITurtleUpgrade> registerTurtleUpgrade(key: ResourceLocation, upgrade: UpgradeType<V>): Supplier<UpgradeType<V>> {
        @Suppress("UNCHECKED_CAST")
        return ForgePeripheralWorks.turtleUpgradeTypes.register(key.path, Supplier { upgrade }) as Supplier<UpgradeType<V>>
    }

    override fun <V : IPocketUpgrade> registerPocketUpgrade(key: ResourceLocation, upgrade: UpgradeType<V>): Supplier<UpgradeType<V>> {
        @Suppress("UNCHECKED_CAST")
        return ForgePeripheralWorks.pocketUpgradeTypes.register(key.path, Supplier { upgrade }) as Supplier<UpgradeType<V>>
    }
}
