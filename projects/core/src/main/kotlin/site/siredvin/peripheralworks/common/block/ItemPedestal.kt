package site.siredvin.peripheralworks.common.block

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralworks.common.blockentity.ItemPedestalBlockEntity

class ItemPedestal : AbstractItemPedestal<ItemPedestalBlockEntity>() {
    override fun newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity = ItemPedestalBlockEntity(blockPos, blockState)

    override fun codec(): MapCodec<out BaseEntityBlock> = RecordCodecBuilder.mapCodec { it.stable(ItemPedestal()) }
}
