package site.siredvin.peripheralworks.common.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import site.siredvin.peripheralium.common.blocks.FacingBlockEntityBlock
import site.siredvin.peripheralium.util.BlockUtil
import site.siredvin.peripheralworks.common.blockentity.UniversalScannerBlockEntity
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import java.util.stream.Stream

class UniversalScanner : FacingBlockEntityBlock<UniversalScannerBlockEntity>(
    { BlockEntityTypes.UNIVERSAL_SCANNER.get() },
    true,
    false,
    BlockUtil.defaultProperties(),
) {

    companion object {
        val SHAPE = Stream.of(
            Block.box(0.0, 0.0, 0.0, 3.0, 16.0, 3.0),
            Block.box(13.0, 0.0, 0.0, 16.0, 16.0, 3.0),
            Block.box(13.0, 0.0, 13.0, 16.0, 16.0, 16.0),
            Block.box(0.0, 0.0, 13.0, 3.0, 16.0, 16.0),
            Block.box(3.0, 0.0, 0.0, 13.0, 3.0, 3.0),
            Block.box(3.0, 0.0, 13.0, 13.0, 3.0, 16.0),
            Block.box(13.0, 0.0, 3.0, 16.0, 3.0, 13.0),
            Block.box(0.0, 0.0, 3.0, 3.0, 3.0, 13.0),
            Block.box(3.0, 13.0, 0.0, 13.0, 16.0, 3.0),
            Block.box(3.0, 13.0, 13.0, 13.0, 16.0, 16.0),
            Block.box(13.0, 13.0, 3.0, 16.0, 16.0, 13.0),
            Block.box(0.0, 13.0, 3.0, 3.0, 16.0, 13.0),
        ).reduce { v1, v2 -> Shapes.join(v1, v2, BooleanOp.OR) }.get()
    }

    override fun getShape(
        blockState: BlockState,
        blockGetter: BlockGetter,
        blockPos: BlockPos,
        collisionContext: CollisionContext,
    ): VoxelShape {
        return SHAPE
    }
}
