package site.siredvin.peripheralworks.common.block

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.Property
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.EntityCollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import site.siredvin.peripheralium.common.blocks.BaseNBTBlock
import site.siredvin.peripheralium.util.BlockUtil
import site.siredvin.peripheralworks.common.blockentity.FlexibleRealityAnchorTileEntity
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.utils.modId

class FlexibleRealityAnchor : BaseNBTBlock<FlexibleRealityAnchorTileEntity>(
    false,
    BlockUtil.decoration().dynamicShape(),
) {
    companion object {
        val CONFIGURED: BooleanProperty = BooleanProperty.create("configured")
        val PLAYER_PASSABLE: BooleanProperty = BooleanProperty.create("player_passable")
        val LIGHT_PASSABLE: BooleanProperty = BooleanProperty.create("light_passable")
        val SKY_LIGHT_PASSABLE: BooleanProperty = BooleanProperty.create("sky_light_passable")
        val INVISIBLE: BooleanProperty = BooleanProperty.create("invisible")

        val SAVABLE_PROPERTIES = listOf(CONFIGURED, PLAYER_PASSABLE, LIGHT_PASSABLE, SKY_LIGHT_PASSABLE, INVISIBLE)

        val BLOCK_MODEL_ID = modId("block/flexible_reality_anchor")
        val ITEM_MODEL_ID = modId("item/flexible_reality_anchor")
    }

    init {
        registerDefaultState(
            getStateDefinition().any()
                .setValue(CONFIGURED, false)
                .setValue(PLAYER_PASSABLE, false)
                .setValue(LIGHT_PASSABLE, false)
                .setValue(SKY_LIGHT_PASSABLE, false)
                .setValue(INVISIBLE, false),
        )
    }

    override val savableProperties: List<Property<*>>
        get() = SAVABLE_PROPERTIES

    override fun newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return FlexibleRealityAnchorTileEntity(blockPos, blockState)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(CONFIGURED)
        builder.add(PLAYER_PASSABLE)
        builder.add(LIGHT_PASSABLE)
        builder.add(SKY_LIGHT_PASSABLE)
        builder.add(INVISIBLE)
    }

    override fun getLightBlock(blockState: BlockState, blockGetter: BlockGetter, blockPos: BlockPos): Int {
        val blockEntity = blockGetter.getBlockEntity(blockPos)
        return if (blockEntity is FlexibleRealityAnchorTileEntity) blockEntity.lightLevel else 0
    }

    override fun createItemStack(): ItemStack {
        return ItemStack(Blocks.FLEXIBLE_REALITY_ANCHOR.get().asItem())
    }

    override fun getShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext,
    ): VoxelShape {
        if (state.getValue(INVISIBLE)) return super.getShape(state, world, pos, context)
        val blockEntity = world.getBlockEntity(pos) as? FlexibleRealityAnchorTileEntity ?: return super.getShape(state, world, pos, context)
        val mimicState = blockEntity.mimic ?: return super.getShape(state, world, pos, context)
        return mimicState.getShape(world, pos)
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return if (state.getValue(INVISIBLE)) RenderShape.INVISIBLE else super.getRenderShape(state)
    }

    override fun useShapeForLightOcclusion(state: BlockState): Boolean {
        return true
    }

    override fun getVisualShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext,
    ): VoxelShape {
        return if (state.getValue(LIGHT_PASSABLE) || !state.getValue(CONFIGURED)) {
            Shapes.empty()
        } else {
            super.getVisualShape(
                state,
                world,
                pos,
                context,
            )
        }
    }

    override fun getShadeBrightness(state: BlockState, world: BlockGetter, pos: BlockPos): Float {
        return if (state.getValue(LIGHT_PASSABLE) || !state.getValue(CONFIGURED)) {
            1.0f
        } else {
            super.getShadeBrightness(
                state,
                world,
                pos,
            )
        }
    }

    override fun propagatesSkylightDown(state: BlockState, world: BlockGetter, pos: BlockPos): Boolean {
        return state.getValue(SKY_LIGHT_PASSABLE) || !state.getValue(CONFIGURED)
    }

    override fun getCollisionShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext,
    ): VoxelShape {
        if (context is EntityCollisionContext && context.entity != null) {
            if (state.getValue(PLAYER_PASSABLE) && context.entity is Player) {
                return Shapes.empty()
            }
        }
        return state.getShape(world, pos)
    }
}
