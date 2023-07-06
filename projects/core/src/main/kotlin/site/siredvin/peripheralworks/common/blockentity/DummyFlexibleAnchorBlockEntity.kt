package site.siredvin.peripheralworks.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralworks.api.IFlexibleRealityAnchorBlockEntity

abstract class DummyFlexibleAnchorBlockEntity(blockEntityType: BlockEntityType<*>, blockPos: BlockPos, state: BlockState) :
    BlockEntity(
        blockEntityType,
        blockPos,
        state,
    ),
    IFlexibleRealityAnchorBlockEntity {
    companion object {
        const val MIMIC_TAG = "mimic"
    }
}
