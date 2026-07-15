package site.siredvin.peripheralworks.integrations.kubejs

import dev.latvian.mods.kubejs.block.BlockBuilder
import dev.latvian.mods.kubejs.block.entity.BlockEntityInfo
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralworks.common.blockentity.DisplayPedestalBlockEntity

class DisplayPedestalBlockEntityInfo(blockBuilder: BlockBuilder) : BlockEntityInfo(blockBuilder) {
    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = DisplayPedestalBlockEntity(pos, state, this.entityType)
}
