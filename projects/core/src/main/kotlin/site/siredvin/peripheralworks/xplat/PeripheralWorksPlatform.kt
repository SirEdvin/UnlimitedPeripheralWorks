package site.siredvin.peripheralworks.xplat

import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.pocket.PocketUpgradeDataProvider
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleUpgradeDataProvider
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import dan200.computercraft.api.upgrades.UpgradeDataProvider.Upgrade
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralium.common.items.DescriptiveBlockItem
import site.siredvin.peripheralworks.PeripheralWorksCore
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Supplier

interface PeripheralWorksPlatform {
    companion object {
        private var _IMPL: PeripheralWorksPlatform? = null

        fun configure(impl: PeripheralWorksPlatform) {
            _IMPL = impl
        }

        fun get(): PeripheralWorksPlatform {
            if (_IMPL == null)
                throw IllegalStateException("You should init PeripheralWorks Platform first")
            return _IMPL!!
        }

        fun <T: Item> registerItem(key: ResourceLocation, item: Supplier<T>): Supplier<T> {
            return get().registerItem(key, item)
        }

        fun <T: Item> registerItem(name: String, item: Supplier<T>): Supplier<T> {
            return registerItem(ResourceLocation(PeripheralWorksCore.MOD_ID, name), item)
        }

        fun <T: Block> registerBlock(key: ResourceLocation, block: Supplier<T>, itemFactory: (T) -> (Item)): Supplier<T> {
            return get().registerBlock(key, block, itemFactory)
        }

        fun <T: Block> registerBlock(name: String, block: Supplier<T>, itemFactory: (T) -> (Item) = { block -> DescriptiveBlockItem(block, Item.Properties()) }): Supplier<T> {
            return get()
                .registerBlock(ResourceLocation(PeripheralWorksCore.MOD_ID, name), block, itemFactory)
        }

        fun <V : BlockEntity, T : BlockEntityType<V>> registerBlockEntity(
            key: ResourceLocation,
            blockEntityTypeSup: Supplier<T>
        ): Supplier<T> {
            return get().registerBlockEntity(key, blockEntityTypeSup)
        }

        fun <T : BlockEntity> createBlockEntityType(
            factory: BiFunction<BlockPos, BlockState, T>,
            block: Block
        ): BlockEntityType<T> {
            return get().createBlockEntityType(factory, block)
        }

        fun <V: ITurtleUpgrade> registerTurtleUpgrade(
            key: ResourceLocation,
            serializer: TurtleUpgradeSerialiser<V>,
            dataGenerator: BiFunction<TurtleUpgradeDataProvider, TurtleUpgradeSerialiser<V>, Upgrade<TurtleUpgradeSerialiser<*>>>,
            postRegistrationHooks: List<Consumer<Supplier<TurtleUpgradeSerialiser<V>>>>
        ) {
            get().registerTurtleUpgrade(key, serializer, dataGenerator, postRegistrationHooks)
        }
        fun <V: IPocketUpgrade> registerPocketUpgrade(
            key: ResourceLocation,
            serializer: PocketUpgradeSerialiser<V>,
            dataGenerator: BiFunction<PocketUpgradeDataProvider, PocketUpgradeSerialiser<V>, Upgrade<PocketUpgradeSerialiser<*>>>
        ) {
            get().registerPocketUpgrade(key, serializer, dataGenerator)
        }

        fun createTurtlesWithUpgrade(upgrade: ITurtleUpgrade): List<ItemStack> {
            return get().createTurtlesWithUpgrade(upgrade)
        }
        fun createPocketsWithUpgrade(upgrade: IPocketUpgrade): List<ItemStack> {
            return get().createPocketsWithUpgrade(upgrade)
        }

        fun isOre(block: BlockState): Boolean {
            return get().isOre(block)
        }
    }

    fun <T: Item> registerItem(key: ResourceLocation, item: Supplier<T>): Supplier<T>

    fun <T: Block> registerBlock(key: ResourceLocation, block: Supplier<T>, itemFactory: (T) -> (Item)): Supplier<T>

    fun <V : BlockEntity, T : BlockEntityType<V>> registerBlockEntity(
        key: ResourceLocation,
        blockEntityTypeSup: Supplier<T>
    ): Supplier<T>

    fun <T : BlockEntity> createBlockEntityType(
        factory: BiFunction<BlockPos, BlockState, T>,
        block: Block
    ): BlockEntityType<T>


    fun <V: ITurtleUpgrade> registerTurtleUpgrade(
        key: ResourceLocation,
        serializer: TurtleUpgradeSerialiser<V>,
        dataGenerator: BiFunction<TurtleUpgradeDataProvider, TurtleUpgradeSerialiser<V>, Upgrade<TurtleUpgradeSerialiser<*>>>,
        postRegistrationHooks: List<Consumer<Supplier<TurtleUpgradeSerialiser<V>>>>
    )
    fun <V: IPocketUpgrade> registerPocketUpgrade(
        key: ResourceLocation,
        serializer: PocketUpgradeSerialiser<V>,
        dataGenerator: BiFunction<PocketUpgradeDataProvider, PocketUpgradeSerialiser<V>, Upgrade<PocketUpgradeSerialiser<*>>>
    )

    fun createTurtlesWithUpgrade(upgrade: ITurtleUpgrade): List<ItemStack>
    fun createPocketsWithUpgrade(upgrade: IPocketUpgrade): List<ItemStack>

    fun isOre(block: BlockState): Boolean
}
