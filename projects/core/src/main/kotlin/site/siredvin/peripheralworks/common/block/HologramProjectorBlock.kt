package site.siredvin.peripheralworks.common.block

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.DirectionProperty
import site.siredvin.broccolium.modules.base.util.BlockUtil
import site.siredvin.peripheralworks.common.blockentity.HologramProjectorBlockEntity
import site.siredvin.peripheralworks.computercraft.peripherals.HologramProjectorPeripheral
import site.siredvin.tweakium.modules.minecraft.block.StatefulPeripheralNBTBlock

class HologramProjectorBlock : StatefulPeripheralNBTBlock<HologramProjectorBlockEntity, HologramProjectorPeripheral>(false, BlockUtil.defaultProperties()) {
    companion object {
        val FACING: DirectionProperty = HorizontalDirectionalBlock.FACING
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH))
    }

    @Deprecated("Deprecated in Java")
    override fun rotate(state: BlockState, rot: Rotation): BlockState = state.setValue(
        FACING,
        rot.rotate(state.getValue(FACING)),
    )

    @Deprecated("Deprecated in Java")
    override fun mirror(state: BlockState, mirrorIn: Mirror): BlockState {
        @Suppress("KotlinRedundantDiagnosticSuppress", "DEPRECATION")
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? = defaultBlockState().setValue(
        FACING,
        context.horizontalDirection.opposite,
    )

    override fun createItemStack(): ItemStack = asItem().defaultInstance

    override fun codec(): MapCodec<out BaseEntityBlock> = RecordCodecBuilder.mapCodec { it.stable(HologramProjectorBlock()) }

    override fun newBlockEntity(
        p0: BlockPos,
        p1: BlockState,
    ): BlockEntity = HologramProjectorBlockEntity(p0, p1)
}
