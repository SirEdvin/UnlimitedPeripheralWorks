package site.siredvin.peripheralworks.fabric

import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.pocket.PocketUpgradeDataProvider
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleUpgradeDataProvider
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import dan200.computercraft.api.upgrades.UpgradeDataProvider
import dan200.computercraft.shared.ModRegistry.Items
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralworks.common.RegistrationQueue
import site.siredvin.peripheralworks.data.ModPocketUpgradeDataProvider
import site.siredvin.peripheralworks.data.ModTurtleUpgradeDataProvider
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Supplier

object FabricPeripheralWorksPlatform: PeripheralWorksPlatform {
    override fun <T : Item> registerItem(key: ResourceLocation, item: Supplier<T>): Supplier<T> {
        val registeredItem = Registry.register(BuiltInRegistries.ITEM, key, item.get())
        return Supplier { registeredItem }
    }

    override fun <T : Block> registerBlock(key: ResourceLocation, block: Supplier<T>, itemFactory: (T) -> Item): Supplier<T> {
        val registeredBlock = Registry.register(BuiltInRegistries.BLOCK, key, block.get())
        Registry.register(BuiltInRegistries.ITEM, key, itemFactory(registeredBlock))
        return Supplier { registeredBlock }
    }

    override fun <V: BlockEntity, T: BlockEntityType<V>> registerBlockEntity(key: ResourceLocation, blockEntityTypeSup: Supplier<T>): Supplier<T> {
        val registeredBlockEntityType = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, blockEntityTypeSup.get())
        return Supplier { registeredBlockEntityType }
    }

    override fun <T : BlockEntity> createBlockEntityType(
        factory: BiFunction<BlockPos, BlockState, T>,
        block: Block
    ): BlockEntityType<T> {
        return FabricBlockEntityTypeBuilder.create({ t: BlockPos, u: BlockState ->
            factory.apply(t, u)
        }).addBlock(block).build()
    }

    private fun <V : ITurtleUpgrade> rawTurtleUpgradeRegistration(
        registry: Registry<TurtleUpgradeSerialiser<*>>, key: ResourceLocation, serializer: TurtleUpgradeSerialiser<V>,
        dataGenerator: BiFunction<TurtleUpgradeDataProvider, TurtleUpgradeSerialiser<V>, UpgradeDataProvider.Upgrade<TurtleUpgradeSerialiser<*>>>,
        postRegistrationHooks: List<Consumer<Supplier<TurtleUpgradeSerialiser<V>>>>
    ): TurtleUpgradeSerialiser<V> {
        val registeredSerializer = Registry.register(registry, key,serializer)

        ModTurtleUpgradeDataProvider.hookUpgrade {
            dataGenerator.apply(it, registeredSerializer)
        }
        val supplier = Supplier {registeredSerializer}
        postRegistrationHooks.forEach { it.accept(supplier) }
        return registeredSerializer
    }

    private fun <V : IPocketUpgrade> rawPocketUpgradeRegistration(
        registry: Registry<PocketUpgradeSerialiser<*>>, key: ResourceLocation, serializer: PocketUpgradeSerialiser<V>,
        dataGenerator: BiFunction<PocketUpgradeDataProvider, PocketUpgradeSerialiser<V>, UpgradeDataProvider.Upgrade<PocketUpgradeSerialiser<*>>>
    ): PocketUpgradeSerialiser<V> {
        val registeredSerializer = Registry.register(registry, key,serializer)

        ModPocketUpgradeDataProvider.hookUpgrade {
            dataGenerator.apply(it, registeredSerializer)
        }
        return registeredSerializer
    }

    override fun <V : ITurtleUpgrade> registerTurtleUpgrade(
        key: ResourceLocation,
        serializer: TurtleUpgradeSerialiser<V>,
        dataGenerator: BiFunction<TurtleUpgradeDataProvider, TurtleUpgradeSerialiser<V>, UpgradeDataProvider.Upgrade<TurtleUpgradeSerialiser<*>>>,
        postRegistrationHooks: List<Consumer<Supplier<TurtleUpgradeSerialiser<V>>>>
    ) {
        val rawTurtleRegistry = BuiltInRegistries.REGISTRY.get(TurtleUpgradeSerialiser.REGISTRY_ID.location())

        if (rawTurtleRegistry == null) {
            RegistrationQueue.scheduleTurtleUpgrade { rawTurtleUpgradeRegistration(it, key, serializer, dataGenerator, postRegistrationHooks) }

        } else {
            val turtleSerializerRegister = rawTurtleRegistry as Registry<TurtleUpgradeSerialiser<*>>
            rawTurtleUpgradeRegistration(turtleSerializerRegister, key, serializer, dataGenerator, postRegistrationHooks)
        }
    }

    override fun <V : IPocketUpgrade> registerPocketUpgrade(
        key: ResourceLocation,
        serializer: PocketUpgradeSerialiser<V>,
        dataGenerator: BiFunction<PocketUpgradeDataProvider, PocketUpgradeSerialiser<V>, UpgradeDataProvider.Upgrade<PocketUpgradeSerialiser<*>>>
    ){
        val rawPocketRegistry = BuiltInRegistries.REGISTRY.get(PocketUpgradeSerialiser.REGISTRY_ID.location())
        if (rawPocketRegistry == null) {
            RegistrationQueue.schedulePocketUpgrade { rawPocketUpgradeRegistration(it, key, serializer, dataGenerator) }
        } else {
            val pocketSerializerRegister = rawPocketRegistry as Registry<PocketUpgradeSerialiser<*>>
            rawPocketUpgradeRegistration(pocketSerializerRegister, key, serializer, dataGenerator)
        }
    }

    override fun createTurtlesWithUpgrade(upgrade: ITurtleUpgrade): List<ItemStack> {
        return listOf(
            Items.TURTLE_NORMAL.get().create(-1, null, -1, null, upgrade, 0, null),
            Items.TURTLE_ADVANCED.get().create(-1, null, -1, null, upgrade, 0, null),
        )
    }

    override fun createPocketsWithUpgrade(upgrade: IPocketUpgrade): List<ItemStack> {
        return listOf(
            Items.POCKET_COMPUTER_NORMAL.get().create(-1, null, -1, upgrade),
            Items.POCKET_COMPUTER_ADVANCED.get().create(-1, null, -1, upgrade),
        )
    }

    override fun isOre(block: BlockState): Boolean {
        return block.`is`(ConventionalBlockTags.ORES)
    }
}