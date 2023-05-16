package site.siredvin.peripheralworks.common.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralworks.common.blockentity.MapPedestalBlockEntity

class MapPedestal: AbstractItemPedestal<MapPedestalBlockEntity>() {
    override fun newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return MapPedestalBlockEntity(blockPos, blockState)
    }
}