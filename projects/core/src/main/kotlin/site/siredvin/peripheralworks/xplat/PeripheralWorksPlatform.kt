package site.siredvin.peripheralworks.xplat

import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import site.siredvin.peripheralium.common.items.DescriptiveBlockItem
import site.siredvin.peripheralium.data.language.ModInformationHolder
import site.siredvin.peripheralworks.PeripheralWorksCore
import java.util.function.Supplier

interface PeripheralWorksPlatform : ModInformationHolder {
    companion object {
        private var _IMPL: PeripheralWorksPlatform? = null
        private val ITEMS: MutableList<Supplier<out Item>> = mutableListOf()
        private val BLOCKS: MutableList<Supplier<out Block>> = mutableListOf()
        private val POCKET_UPGRADES: MutableList<Supplier<PocketUpgradeSerialiser<out IPocketUpgrade>>> = mutableListOf()
        private val TURTLE_UPGRADES: MutableList<Supplier<TurtleUpgradeSerialiser<out ITurtleUpgrade>>> = mutableListOf()

        val holder: ModInformationHolder
            get() = get()

        fun configure(impl: PeripheralWorksPlatform) {
            _IMPL = impl
        }

        private fun get(): PeripheralWorksPlatform {
            if (_IMPL == null) {
                throw IllegalStateException("You should init PeripheralWorks Platform first")
            }
            return _IMPL!!
        }

        fun <T : Item> registerItem(key: ResourceLocation, item: Supplier<T>): Supplier<T> {
            val registeredItem = get().registerItem(key, item)
            ITEMS.add(registeredItem)
            return registeredItem
        }

        fun <T : Item> registerItem(name: String, item: Supplier<T>): Supplier<T> {
            return registerItem(ResourceLocation(PeripheralWorksCore.MOD_ID, name), item)
        }

        fun <T : Block> registerBlock(key: ResourceLocation, block: Supplier<T>, itemFactory: (T) -> (Item)): Supplier<T> {
            return get().registerBlock(key, block, itemFactory)
        }

        fun <T : Block> registerBlock(name: String, block: Supplier<T>, itemFactory: (T) -> (Item) = { block -> DescriptiveBlockItem(block, Item.Properties()) }): Supplier<T> {
            val registeredBlock = get()
                .registerBlock(ResourceLocation(PeripheralWorksCore.MOD_ID, name), block, itemFactory)
            BLOCKS.add(registeredBlock)
            return registeredBlock
        }

        fun <V : BlockEntity, T : BlockEntityType<V>> registerBlockEntity(
            key: ResourceLocation,
            blockEntityTypeSup: Supplier<T>,
        ): Supplier<T> {
            return get().registerBlockEntity(key, blockEntityTypeSup)
        }

        fun registerCreativeTab(key: ResourceLocation, tab: CreativeModeTab): Supplier<CreativeModeTab> {
            return get().registerCreativeTab(key, tab)
        }

        fun <V : ITurtleUpgrade> registerTurtleUpgrade(
            name: String,
            serializer: TurtleUpgradeSerialiser<V>,
        ): Supplier<TurtleUpgradeSerialiser<V>> {
            return registerTurtleUpgrade(ResourceLocation(PeripheralWorksCore.MOD_ID, name), serializer)
        }

        fun <V : ITurtleUpgrade> registerTurtleUpgrade(
            key: ResourceLocation,
            serializer: TurtleUpgradeSerialiser<V>,
        ): Supplier<TurtleUpgradeSerialiser<V>> {
            val registered = get().registerTurtleUpgrade(key, serializer)
            TURTLE_UPGRADES.add(registered as Supplier<TurtleUpgradeSerialiser<out ITurtleUpgrade>>)
            return registered
        }

        fun <V : IPocketUpgrade> registerPocketUpgrade(
            name: String,
            serializer: PocketUpgradeSerialiser<V>,
        ): Supplier<PocketUpgradeSerialiser<V>> {
            return registerPocketUpgrade(ResourceLocation(PeripheralWorksCore.MOD_ID, name), serializer)
        }

        fun <V : IPocketUpgrade> registerPocketUpgrade(
            key: ResourceLocation,
            serializer: PocketUpgradeSerialiser<V>,
        ): Supplier<PocketUpgradeSerialiser<V>> {
            val registered = get().registerPocketUpgrade(key, serializer)
            POCKET_UPGRADES.add(registered as Supplier<PocketUpgradeSerialiser<out IPocketUpgrade>>)
            return registered
        }
    }

    override val blocks: List<Supplier<out Block>>
        get() = BLOCKS

    override val items: List<Supplier<out Item>>
        get() = ITEMS

    override val turtleSerializers: List<Supplier<TurtleUpgradeSerialiser<out ITurtleUpgrade>>>
        get() = TURTLE_UPGRADES

    override val pocketSerializers: List<Supplier<PocketUpgradeSerialiser<out IPocketUpgrade>>>
        get() = POCKET_UPGRADES

    fun <T : Item> registerItem(key: ResourceLocation, item: Supplier<T>): Supplier<T>

    fun <T : Block> registerBlock(key: ResourceLocation, block: Supplier<T>, itemFactory: (T) -> (Item)): Supplier<T>

    fun registerCreativeTab(key: ResourceLocation, tab: CreativeModeTab): Supplier<CreativeModeTab>

    fun <V : BlockEntity, T : BlockEntityType<V>> registerBlockEntity(
        key: ResourceLocation,
        blockEntityTypeSup: Supplier<T>,
    ): Supplier<T>

    fun <V : ITurtleUpgrade> registerTurtleUpgrade(
        key: ResourceLocation,
        serializer: TurtleUpgradeSerialiser<V>,
    ): Supplier<TurtleUpgradeSerialiser<V>>

    fun <V : IPocketUpgrade> registerPocketUpgrade(
        key: ResourceLocation,
        serializer: PocketUpgradeSerialiser<V>,
    ): Supplier<PocketUpgradeSerialiser<V>>
}
