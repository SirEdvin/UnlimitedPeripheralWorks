package site.siredvin.peripheralworks.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.computercraft.peripherals.ItemPedestalPeripheral
import java.util.function.Predicate

class ItemPedestalBlockEntity(blockPos: BlockPos, blockState: BlockState, blockEntityType: BlockEntityType<*> = BlockEntityTypes.ITEM_PEDESTAL.get(), holdingStacks: Int = 1) :
    AbstractItemPedestalBlockEntity<ItemPedestalPeripheral>(
        blockEntityType,
        blockPos,
        blockState,
        holdingStacks,
    ) {

    override val itemFilter: Predicate<ItemStack> = Predicate { true }

    override fun createPeripheral(side: Direction): ItemPedestalPeripheral = ItemPedestalPeripheral(this)
}
