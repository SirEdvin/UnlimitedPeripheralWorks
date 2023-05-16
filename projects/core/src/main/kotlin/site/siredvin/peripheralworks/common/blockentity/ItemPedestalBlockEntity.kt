package site.siredvin.peripheralworks.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralworks.api.IItemStackHolder
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.computercraft.peripherals.ItemPedestalPeripheral
import java.util.function.Predicate

class ItemPedestalBlockEntity(blockPos: BlockPos, blockState: BlockState) : AbstractItemPedestalBlockEntity<ItemPedestalPeripheral>(
    BlockEntityTypes.ITEM_PEDESTAL.get(), blockPos, blockState
), IItemStackHolder, Container {

    override val itemFilter: Predicate<ItemStack> = Predicate { true }

    override fun createPeripheral(side: Direction): ItemPedestalPeripheral {
        return ItemPedestalPeripheral(this)
    }
}