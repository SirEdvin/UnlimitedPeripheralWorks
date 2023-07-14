package site.siredvin.peripheralworks.forge

import dan200.computercraft.api.pocket.IPocketUpgrade
import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import dan200.computercraft.api.turtle.ITurtleUpgrade
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.Container
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import site.siredvin.peripheralworks.ForgePeripheralWorks
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform
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

    override fun registerCreativeTab(key: ResourceLocation, tab: CreativeModeTab): Supplier<CreativeModeTab> {
        return ForgePeripheralWorks.creativeTabRegistry.register(key.path) { tab }
    }

    override fun <C : Container, T : Recipe<C>> registerRecipeSerializer(key: ResourceLocation, serializer: RecipeSerializer<T>): Supplier<RecipeSerializer<T>> {
        return ForgePeripheralWorks.recipeSerializers.register(key.path) { serializer }
    }

    override fun <V : IPocketUpgrade> registerPocketUpgrade(
        key: ResourceLocation,
        serializer: PocketUpgradeSerialiser<V>,
    ): Supplier<PocketUpgradeSerialiser<V>> {
        return ForgePeripheralWorks.pocketSerializers.register(key.path) { serializer }
    }

    override fun <V : ITurtleUpgrade> registerTurtleUpgrade(
        key: ResourceLocation,
        serializer: TurtleUpgradeSerialiser<V>,
    ): Supplier<TurtleUpgradeSerialiser<V>> {
        return ForgePeripheralWorks.turtleSerializers.register(key.path) { serializer }
    }
}
