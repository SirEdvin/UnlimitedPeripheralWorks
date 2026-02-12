package site.siredvin.peripheralworks.common.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.broccolium.modules.base.util.BlockUtil
import site.siredvin.peripheralworks.common.blockentity.ItemPedestalBlockEntity

class ItemPedestal(properties: Properties = BlockUtil.defaultProperties()) : AbstractItemPedestal<ItemPedestalBlockEntity>(properties) {
    override fun newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity = ItemPedestalBlockEntity(blockPos, blockState)
}
