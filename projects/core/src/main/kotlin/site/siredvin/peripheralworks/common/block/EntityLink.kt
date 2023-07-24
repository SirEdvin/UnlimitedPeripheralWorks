package site.siredvin.peripheralworks.common.block

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import site.siredvin.peripheralium.common.blocks.FacingBlockEntityBlock
import site.siredvin.peripheralium.util.BlockUtil
import site.siredvin.peripheralworks.common.blockentity.EntityLinkBlockEntity
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes

class EntityLink : FacingBlockEntityBlock<EntityLinkBlockEntity>({ BlockEntityTypes.ENTITY_LINK.get() }, true, true, BlockUtil.defaultProperties()) {
    companion object {
        val CONFIGURED: BooleanProperty = BooleanProperty.create("configured")
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(CONFIGURED, false))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(CONFIGURED)
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
}
