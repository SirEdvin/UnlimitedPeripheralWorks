package site.siredvin.peripheralworks.common.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState

class CustomPedestal<T : BlockEntity>(properties: BlockBehaviour.Properties, private val blockEntityProvider: (blockPos: BlockPos, blockState: BlockState) -> T) : AbstractItemPedestal<T>(properties) {
    override fun newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity = blockEntityProvider(blockPos, blockState)
}
