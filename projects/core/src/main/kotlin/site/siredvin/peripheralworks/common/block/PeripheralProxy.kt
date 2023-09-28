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
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import site.siredvin.peripheralium.common.blocks.BaseTileEntityBlock
import site.siredvin.peripheralium.util.BlockUtil
import site.siredvin.peripheralworks.common.blockentity.PeripheralProxyBlockEntity
import java.util.stream.Stream

class PeripheralProxy : BaseTileEntityBlock<PeripheralProxyBlockEntity>(true, BlockUtil.defaultProperties()) {
    companion object {
        val ORIENTATION: DirectionProperty = DirectionProperty.create("orientation")
        val SHAPE = Stream.of(
            Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            Block.box(2.0, 2.0, 2.0, 14.0, 4.0, 14.0),
            Block.box(4.0, 4.0, 4.0, 12.0, 6.0, 12.0),
        ).reduce { v1, v2 -> Shapes.join(v1, v2, BooleanOp.OR) }.get()
        val SHAPE_SOUTH = Stream.of(
            box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0),
            box(2.0, 2.0, 2.0, 14.0, 14.0, 4.0),
            box(4.0, 4.0, 4.0, 12.0, 12.0, 6.0),
        ).reduce { v1, v2 -> Shapes.join(v1, v2, BooleanOp.OR) }.get()
        val SHAPE_NORTH = Stream.of(
            Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0),
            Block.box(2.0, 2.0, 12.0, 14.0, 14.0, 14.0),
            Block.box(4.0, 4.0, 10.0, 12.0, 12.0, 12.0),
        ).reduce { v1, v2 -> Shapes.join(v1, v2, BooleanOp.OR) }.get()
        val SHAPE_WEST = Stream.of(
            Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0),
            Block.box(12.0, 2.0, 2.0, 14.0, 14.0, 14.0),
            Block.box(10.0, 4.0, 4.0, 12.0, 12.0, 12.0),
        ).reduce { v1, v2 -> Shapes.join(v1, v2, BooleanOp.OR) }.get()
        val SHAPE_EAST = Stream.of(
            Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0),
            Block.box(2.0, 2.0, 2.0, 4.0, 14.0, 14.0),
            Block.box(4.0, 4.0, 4.0, 6.0, 12.0, 12.0),
        ).reduce { v1, v2 -> Shapes.join(v1, v2, BooleanOp.OR) }.get()
        val SHAPE_DOWN = Stream.of(
            Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0),
            Block.box(2.0, 12.0, 2.0, 14.0, 14.0, 14.0),
            Block.box(4.0, 10.0, 4.0, 12.0, 12.0, 12.0),
        ).reduce { v1, v2 -> Shapes.join(v1, v2, BooleanOp.OR) }.get()
    }

    init {
        registerDefaultState(getStateDefinition().any().setValue(ORIENTATION, Direction.UP))
    }

    override fun newBlockEntity(p0: BlockPos, p1: BlockState): BlockEntity {
        return PeripheralProxyBlockEntity(p0, p1)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(ORIENTATION)
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
    override fun mirror(state: BlockState, mirror: Mirror): BlockState {
        return state.rotate(mirror.getRotation(state.getValue(ORIENTATION)))
    }

    @Deprecated("Deprecated in Java")
    override fun rotate(state: BlockState, rotation: Rotation): BlockState {
        return state.setValue(ORIENTATION, rotation.rotate(state.getValue(ORIENTATION)))
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        return defaultBlockState().setValue(ORIENTATION, context.clickedFace)
    }

    @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
    @Deprecated("Deprecated in Java")
    override fun getShape(
        state: BlockState,
        blockGetter: BlockGetter,
        blockPos: BlockPos,
        collisionContext: CollisionContext,
    ): VoxelShape {
        return when (state.getValue(ORIENTATION)) {
            Direction.DOWN -> SHAPE_DOWN
            Direction.UP -> SHAPE
            Direction.NORTH -> SHAPE_NORTH
            Direction.SOUTH -> SHAPE_SOUTH
            Direction.WEST -> SHAPE_WEST
            Direction.EAST -> SHAPE_EAST
        }
    }
}
