package site.siredvin.peripheralworks.common.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import site.siredvin.peripheralium.common.blocks.BaseTileEntityBlock
import java.util.stream.Stream

abstract class BasePedestal<T : BlockEntity>(properties: Properties): BaseTileEntityBlock<T>(false, properties) {
    init {
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.UP))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(FACING)
    }

    override fun mirror(state: BlockState, mirror: Mirror): BlockState {
        return state.rotate(mirror.getRotation(state.getValue(FACING)))
    }

    override fun rotate(state: BlockState, rotation: Rotation): BlockState {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)))
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        return defaultBlockState().setValue(FACING, context.clickedFace)
    }

    override fun getShape(
        state: BlockState,
        blockGetter: BlockGetter,
        blockPos: BlockPos,
        collisionContext: CollisionContext
    ): VoxelShape {
        return when (state.getValue(FACING)) {
            Direction.SOUTH -> SOUTH_PEDESTAL
            Direction.NORTH -> NORTH_PEDESTAL
            Direction.EAST -> EAST_PEDESTAL
            Direction.WEST -> WEST_PEDESTAL
            Direction.DOWN -> DOWN_PEDESTAL
            else -> PEDESTAL
        }
    }

    companion object {
        val FACING: DirectionProperty = BlockStateProperties.FACING
        private val PEDESTAL = Stream.of(
            box(3.0, 0.0, 3.0, 13.0, 1.0, 13.0),
            box(6.0, 1.0, 6.0, 10.0, 9.0, 10.0),
            box(4.0, 9.0, 4.0, 12.0, 10.0, 12.0),
            box(5.0, 10.0, 5.0, 11.0, 11.0, 11.0)
        ).reduce { v1: VoxelShape, v2: VoxelShape -> Shapes.join(v1, v2, BooleanOp.OR) }.get()
        private val DOWN_PEDESTAL = Stream.of(
            box(3.0, 15.0, 3.0, 13.0, 16.0, 13.0),
            box(6.0, 7.0, 6.0, 10.0, 15.0, 10.0),
            box(4.0, 6.0, 4.0, 12.0, 7.0, 12.0),
            box(5.0, 5.0, 5.0, 11.0, 6.0, 11.0)
        ).reduce { v1: VoxelShape, v2: VoxelShape -> Shapes.join(v1, v2, BooleanOp.OR) }.get()
        private val SOUTH_PEDESTAL = Stream.of(
            box(3.0, 3.0, 0.0, 13.0, 13.0, 1.0),
            box(6.0, 6.0, 1.0, 10.0, 10.0, 9.0),
            box(4.0, 4.0, 9.0, 12.0, 12.0, 10.0),
            box(5.0, 5.0, 10.0, 11.0, 11.0, 11.0)
        ).reduce { v1: VoxelShape, v2: VoxelShape -> Shapes.join(v1, v2, BooleanOp.OR) }.get()
        private val NORTH_PEDESTAL = Stream.of(
            box(3.0, 3.0, 15.0, 13.0, 13.0, 16.0),
            box(6.0, 6.0, 7.0, 10.0, 10.0, 15.0),
            box(4.0, 4.0, 6.0, 12.0, 12.0, 7.0),
            box(5.0, 5.0, 5.0, 11.0, 11.0, 6.0)
        ).reduce { v1: VoxelShape, v2: VoxelShape -> Shapes.join(v1, v2, BooleanOp.OR) }.get()
        private val WEST_PEDESTAL = Stream.of(
            box(15.0, 3.0, 3.0, 16.0, 13.0, 13.0),
            box(7.0, 6.0, 6.0, 15.0, 10.0, 10.0),
            box(6.0, 4.0, 4.0, 7.0, 12.0, 12.0),
            box(5.0, 5.0, 5.0, 6.0, 11.0, 11.0)
        ).reduce { v1: VoxelShape, v2: VoxelShape -> Shapes.join(v1, v2, BooleanOp.OR) }.get()
        private val EAST_PEDESTAL = Stream.of(
            box(0.0, 3.0, 3.0, 1.0, 13.0, 13.0),
            box(1.0, 6.0, 6.0, 9.0, 10.0, 10.0),
            box(9.0, 4.0, 4.0, 10.0, 12.0, 12.0),
            box(10.0, 5.0, 5.0, 11.0, 11.0, 11.0)
        ).reduce { v1: VoxelShape, v2: VoxelShape -> Shapes.join(v1, v2, BooleanOp.OR) }.get()
    }
}