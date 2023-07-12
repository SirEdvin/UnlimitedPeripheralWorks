package site.siredvin.peripheralworks.common.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.Property
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import site.siredvin.peripheralium.common.blocks.BaseNBTBlock
import site.siredvin.peripheralium.util.BlockUtil
import site.siredvin.peripheralworks.common.blockentity.FlexibleStatueBlockEntity
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.utils.modId

class FlexibleStatue :
    BaseNBTBlock<FlexibleStatueBlockEntity>(false, BlockUtil.decoration().dynamicShape()) {
    companion object {
        val WHITE_TEXTURE = modId("block/white")
        val CONFIGURED = BooleanProperty.create("configured")
        val FACING = BlockStateProperties.HORIZONTAL_FACING

        val BLOCK_MODEL_ID = modId("block/flexible_statue")
        val ITEM_MODEL_ID = modId("item/flexible_statue")

        val SAVABLE_PROPERTIES = listOf(
            CONFIGURED,
        )
    }

    init {
        this.registerDefaultState(
            this.getStateDefinition().any()
                .setValue(CONFIGURED, false)
                .setValue(FACING, Direction.SOUTH),
        )
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(CONFIGURED)
        builder.add(FACING)
    }

    override val savableProperties: List<Property<*>>
        get() = SAVABLE_PROPERTIES

    override fun newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return BlockEntityTypes.FLEXIBLE_STATUE.get().create(blockPos, blockState)!!
    }

    override fun createItemStack(): ItemStack {
        return ItemStack(Blocks.FLEXIBLE_STATUE.get().asItem())
    }

    override fun getShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext,
    ): VoxelShape {
        val blockEntity = world.getBlockEntity(pos) as? FlexibleStatueBlockEntity ?: return super.getShape(state, world, pos, context)
        return blockEntity.blockShape ?: super.getShape(state, world, pos, context)
    }

    override fun useShapeForLightOcclusion(blockState: BlockState): Boolean {
        return true
    }

    override fun getLightBlock(blockState: BlockState, blockGetter: BlockGetter, blockPos: BlockPos): Int {
        val blockEntity = blockGetter.getBlockEntity(blockPos) as? FlexibleStatueBlockEntity ?: return super.getLightBlock(blockState, blockGetter, blockPos)
        return blockEntity.lightLevel
    }

    override fun rotate(state: BlockState, rot: Rotation): BlockState {
        return state.setValue(
            FACING,
            rot.rotate(state.getValue(FACING)),
        )
    }

    override fun mirror(state: BlockState, mirrorIn: Mirror): BlockState {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)))
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState {
        return defaultBlockState().setValue(
            FACING,
            context.horizontalDirection.opposite,
        )
    }
}
