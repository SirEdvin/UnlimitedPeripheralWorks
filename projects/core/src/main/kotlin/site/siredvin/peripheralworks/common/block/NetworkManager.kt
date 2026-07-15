package site.siredvin.peripheralworks.common.block

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import site.siredvin.broccolium.modules.base.block.FacingBlockEntityBlock
import site.siredvin.peripheralworks.common.blockentity.NetworkManagerBlockEntity

class NetworkManager : FacingBlockEntityBlock<NetworkManagerBlockEntity>(true, false) {
    companion object {
        val TOGGLING: BooleanProperty = BooleanProperty.create("useless_toggling")
        val CONNECTED: BooleanProperty = BooleanProperty.create("connected")
    }

    init {
        registerDefaultState(getStateDefinition().any().setValue(TOGGLING, false).setValue(CONNECTED, false).setValue(FACING, Direction.SOUTH))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(TOGGLING)
        builder.add(CONNECTED)
    }

    override fun newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity = NetworkManagerBlockEntity(blockPos, blockState)

    override fun codec(): MapCodec<out BaseEntityBlock> = RecordCodecBuilder.mapCodec { it.stable(NetworkManager()) }
}
