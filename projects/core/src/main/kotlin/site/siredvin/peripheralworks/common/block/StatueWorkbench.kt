package site.siredvin.peripheralworks.common.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import site.siredvin.peripheralium.common.blocks.BaseTileEntityBlock
import site.siredvin.peripheralium.util.BlockUtil
import site.siredvin.peripheralworks.common.blockentity.StatueWorkbenchBlockEntity
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.common.setup.Blocks

class StatueWorkbench : BaseTileEntityBlock<StatueWorkbenchBlockEntity>(false, BlockUtil.defaultProperties()) {

    companion object {
        val CONNECTED: BooleanProperty = BooleanProperty.create("connected")
    }

    override fun newBlockEntity(p0: BlockPos, p1: BlockState): BlockEntity? {
        return BlockEntityTypes.STATUE_WORKBENCH.get().create(p0, p1)
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(CONNECTED, false))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(CONNECTED)
    }

    fun pushChangeToClient(level: Level, pos: BlockPos, oldState: BlockState, newState: BlockState) {
        level.setBlockAndUpdate(pos, newState)
        level.sendBlockUpdated(pos, oldState, newState, Block.UPDATE_ALL)
    }

    @Deprecated("Deprecated in Java")
    override fun neighborChanged(
        blockState: BlockState,
        level: Level,
        blockPos: BlockPos,
        neighbourBlock: Block,
        neighbourPos: BlockPos,
        bl: Boolean,
    ) {
        if (blockPos.above() == neighbourPos) {
            val isFlexibleStatue = level.getBlockState(neighbourPos).`is`(Blocks.FLEXIBLE_STATUE.get())
            if (blockState.getValue(CONNECTED) && !isFlexibleStatue) {
                pushChangeToClient(level, blockPos, blockState, blockState.setValue(CONNECTED, false))
            }
            if (!blockState.getValue(CONNECTED) && isFlexibleStatue) {
                pushChangeToClient(level, blockPos, blockState, blockState.setValue(CONNECTED, true))
            }
        }
        @Suppress("DEPRECATION")
        super.neighborChanged(blockState, level, blockPos, neighbourBlock, neighbourPos, bl)
    }
}
