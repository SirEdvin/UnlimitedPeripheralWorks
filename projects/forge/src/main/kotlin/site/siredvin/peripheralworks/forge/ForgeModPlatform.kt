package site.siredvin.peripheralworks.forge

import dan200.computercraft.api.pocket.PocketUpgradeSerialiser
import dan200.computercraft.api.turtle.TurtleUpgradeSerialiser
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.registries.DeferredRegister
import site.siredvin.peripheralium.forge.ForgeBaseInnerPlatform
import site.siredvin.peripheralworks.ForgePeripheralWorks
import site.siredvin.peripheralworks.PeripheralWorksCore

object ForgeModPlatform : ForgeBaseInnerPlatform() {
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
