package site.siredvin.peripheralworks.fabric

import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform
import java.util.function.Supplier

object FabricPeripheralWorksPlatform : PeripheralWorksPlatform {
    override fun <T : Item> registerItem(key: ResourceLocation, item: Supplier<T>): Supplier<T> {
        val registeredItem = Registry.register(BuiltInRegistries.ITEM, key, item.get())
        return Supplier { registeredItem }
    }

    override fun <T : Block> registerBlock(key: ResourceLocation, block: Supplier<T>, itemFactory: (T) -> Item): Supplier<T> {
        val registeredBlock = Registry.register(BuiltInRegistries.BLOCK, key, block.get())
        Registry.register(BuiltInRegistries.ITEM, key, itemFactory(registeredBlock))
        return Supplier { registeredBlock }
    }

    override fun <V : BlockEntity, T : BlockEntityType<V>> registerBlockEntity(key: ResourceLocation, blockEntityTypeSup: Supplier<T>): Supplier<T> {
        val registeredBlockEntityType = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, blockEntityTypeSup.get())
        return Supplier { registeredBlockEntityType }
    }

    override fun registerCreativeTab(key: ResourceLocation, tab: CreativeModeTab): Supplier<CreativeModeTab> {
        val registeredTab = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, tab)
        return Supplier { registeredTab }
    }

    override fun <V : IPocketUpgrade> registerPocketUpgrade(
        key: ResourceLocation,
        serializer: PocketUpgradeSerialiser<V>,
    ): Supplier<PocketUpgradeSerialiser<V>> {
        val registry: Registry<PocketUpgradeSerialiser<*>> = (
            BuiltInRegistries.REGISTRY.get(PocketUpgradeSerialiser.registryId().location())
                ?: throw IllegalStateException("Something is not correct with turtle registry")
            ) as Registry<PocketUpgradeSerialiser<*>>
        val registered = Registry.register(registry, key, serializer)
        return Supplier { registered }
    }

    override fun <V : ITurtleUpgrade> registerTurtleUpgrade(
        key: ResourceLocation,
        serializer: TurtleUpgradeSerialiser<V>,
    ): Supplier<TurtleUpgradeSerialiser<V>> {
        val registry: Registry<TurtleUpgradeSerialiser<*>> = (
            BuiltInRegistries.REGISTRY.get(TurtleUpgradeSerialiser.registryId().location())
                ?: throw IllegalStateException("Something is not correct with turtle registry")
            ) as Registry<TurtleUpgradeSerialiser<*>>
        val registered = Registry.register(registry, key, serializer)
        return Supplier { registered }
    }
}
