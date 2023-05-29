package site.siredvin.peripheralworks.common.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralworks.common.blockentity.ItemPedestalBlockEntity

class ItemPedestal : AbstractItemPedestal<ItemPedestalBlockEntity>() {
    override fun newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return ItemPedestalBlockEntity(blockPos, blockState)
    }
}
