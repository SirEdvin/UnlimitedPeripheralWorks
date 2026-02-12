package site.siredvin.peripheralworks.common.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.broccolium.modules.base.util.BlockUtil
import site.siredvin.peripheralworks.common.blockentity.MapPedestalBlockEntity

class MapPedestal(properties: Properties = BlockUtil.defaultProperties()) : AbstractItemPedestal<MapPedestalBlockEntity>(properties) {
    override fun newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity = MapPedestalBlockEntity(blockPos, blockState)
}
