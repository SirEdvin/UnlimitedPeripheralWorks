package site.siredvin.peripheralworks.forge

import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.pocket.PocketUpgradeDataProvider
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleUpgradeDataProvider
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import dan200.computercraft.api.upgrades.UpgradeDataProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import site.siredvin.peripheralworks.ForgePeripheralWorks
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.data.ModPocketUpgradeDataProvider
import site.siredvin.peripheralworks.data.ModTurtleUpgradeDataProvider
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Supplier

object ForgePeripheralWorksPlatform : PeripheralWorksPlatform {
    override fun <T : Item> registerItem(key: ResourceLocation, item: Supplier<T>): Supplier<T> {
        return ForgePeripheralWorks.itemsRegistry.register(key.path, item)
    }

    override fun <T : Block> registerBlock(key: ResourceLocation, block: Supplier<T>, itemFactory: (T) -> Item): Supplier<T> {
        PeripheralWorksCore.LOGGER.warn("Register block")
        val blockRegister = ForgePeripheralWorks.blocksRegistry.register(key.path, block)
        ForgePeripheralWorks.itemsRegistry.register(key.path) { itemFactory(blockRegister.get()) }
        return blockRegister
    }

    override fun <V : BlockEntity, T : BlockEntityType<V>> registerBlockEntity(
        key: ResourceLocation,
        blockEntityTypeSup: Supplier<T>,
    ): Supplier<T> {
        return ForgePeripheralWorks.blockEntityTypesRegistry.register(key.path, blockEntityTypeSup)
    }

    override fun <V : ITurtleUpgrade> registerTurtleUpgrade(
        key: ResourceLocation,
        serializer: TurtleUpgradeSerialiser<V>,
        dataGenerator: BiFunction<TurtleUpgradeDataProvider, TurtleUpgradeSerialiser<V>, UpgradeDataProvider.Upgrade<TurtleUpgradeSerialiser<*>>>,
        postRegistrationHooks: List<Consumer<Supplier<TurtleUpgradeSerialiser<V>>>>,
    ) {
        val turtleUpgrade = ForgePeripheralWorks.turtleSerializers.register(key.path) { serializer }
        ModTurtleUpgradeDataProvider.hookUpgrade { dataGenerator.apply(it, turtleUpgrade.get()) }
        postRegistrationHooks.forEach { it.accept(turtleUpgrade) }
    }

    override fun <V : IPocketUpgrade> registerPocketUpgrade(
        key: ResourceLocation,
        serializer: PocketUpgradeSerialiser<V>,
        dataGenerator: BiFunction<PocketUpgradeDataProvider, PocketUpgradeSerialiser<V>, UpgradeDataProvider.Upgrade<PocketUpgradeSerialiser<*>>>,
    ) {
        val pocketUpgrade = ForgePeripheralWorks.pocketSerializers.register(key.path) { serializer }
        ModPocketUpgradeDataProvider.hookUpgrade { dataGenerator.apply(it, pocketUpgrade.get()) }
    }
}
