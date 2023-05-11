package site.siredvin.peripheralworks.xplat

import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.pocket.PocketUpgradeDataProvider
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleUpgradeDataProvider
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import dan200.computercraft.api.upgrades.UpgradeDataProvider
import dan200.computercraft.api.upgrades.UpgradeDataProvider.Upgrade
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
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
    }

    fun <T: Item> registerItem(key: ResourceLocation, item: Supplier<T>): Supplier<T>

    fun <T: Block> registerBlock(key: ResourceLocation, block: Supplier<T>, itemFactory: (T) -> (Item)): Supplier<T>

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
}
