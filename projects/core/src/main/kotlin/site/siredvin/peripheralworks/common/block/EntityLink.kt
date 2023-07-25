package site.siredvin.peripheralworks.common.block

import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import site.siredvin.peripheralium.common.blocks.FacingBlockEntityBlock
import site.siredvin.peripheralium.util.BlockUtil
import site.siredvin.peripheralworks.common.blockentity.EntityLinkBlockEntity
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import java.util.stream.Stream

class EntityLink : FacingBlockEntityBlock<EntityLinkBlockEntity>({ BlockEntityTypes.ENTITY_LINK.get() }, true, true, BlockUtil.defaultProperties()) {
    companion object {
        val CONFIGURED: BooleanProperty = BooleanProperty.create("configured")
        val ENTITY_FOUND: BooleanProperty = BooleanProperty.create("entity_found")
        val SHAPE = Stream.of(
            box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
            box(0.0, 10.0, 0.0, 16.0, 13.0, 3.0),
            box(0.0, 10.0, 3.0, 3.0, 13.0, 13.0),
            box(13.0, 10.0, 3.0, 16.0, 13.0, 13.0),
            box(0.0, 10.0, 13.0, 16.0, 13.0, 16.0),
        ).reduce { v1: VoxelShape, v2: VoxelShape ->
            Shapes.or(
                v1,
                v2,
            )
        }.get()
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(CONFIGURED, false).setValue(ENTITY_FOUND, false))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(CONFIGURED)
        builder.add(ENTITY_FOUND)
    }

    @Deprecated("Deprecated in Java")
    override fun use(
        blockState: BlockState,
        level: Level,
        blockPos: BlockPos,
        player: Player,
        interactionHand: InteractionHand,
        blockHitResult: BlockHitResult,
    ): InteractionResult {
        if (interactionHand == InteractionHand.MAIN_HAND) {
            val blockEntity = level.getBlockEntity(blockPos)
            if (blockEntity is EntityLinkBlockEntity && !blockEntity.storedStack.isEmpty) {
                val itemInHand = player.getItemInHand(interactionHand)
                if (itemInHand.isEmpty) {
                    player.setItemInHand(interactionHand, blockEntity.storedStack)
                    blockEntity.storedStack = ItemStack.EMPTY
                    return InteractionResult.SUCCESS
                }
            }
        }
        @Suppress("DEPRECATION")
        return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult)
    }

    override fun playerWillDestroy(level: Level, pos: BlockPos, state: BlockState, player: Player) {
        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity is EntityLinkBlockEntity) {
            if (!level.isClientSide && !blockEntity.storedStack.isEmpty) {
                val itemDrop = ItemEntity(
                    level,
                    pos.x.toDouble() + 0.5,
                    pos.y.toDouble() + 0.5,
                    pos.z.toDouble() + 0.5,
                    blockEntity.storedStack,
                )
                itemDrop.setDefaultPickUpDelay()
                level.addFreshEntity(itemDrop)
            }
        }
        super.playerWillDestroy(level, pos, state, player)
    }

    @Deprecated("Deprecated in Java")
    override fun getShape(blockState: BlockState, blockGetter: BlockGetter, blockPos: BlockPos, collisionContext: CollisionContext): VoxelShape {
        return SHAPE
    }
}
