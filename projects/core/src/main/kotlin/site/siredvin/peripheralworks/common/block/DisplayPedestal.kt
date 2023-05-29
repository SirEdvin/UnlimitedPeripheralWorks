package site.siredvin.peripheralworks.common.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import site.siredvin.peripheralium.util.BlockUtil
import site.siredvin.peripheralium.util.representation.LuaRepresentation
import site.siredvin.peripheralium.util.representation.RepresentationMode
import site.siredvin.peripheralworks.common.blockentity.DisplayPedestalBlockEntity

class DisplayPedestal : BasePedestal<DisplayPedestalBlockEntity>(BlockUtil.defaultProperties()) {

    override fun use(
        blockState: BlockState,
        level: Level,
        blockPos: BlockPos,
        player: Player,
        interactionHand: InteractionHand,
        blockHitResult: BlockHitResult,
    ): InteractionResult {
        val itemInHand = player.getItemInHand(interactionHand)
        val blockEntity = level.getBlockEntity(blockPos)
        if (blockEntity is DisplayPedestalBlockEntity) {
            blockEntity.getPeripheral(Direction.EAST)?.queueEvent(
                "pedestal_right_click",
                LuaRepresentation.forItemStack(itemInHand, RepresentationMode.FULL),
            )
            return InteractionResult.SUCCESS
        }
        return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult)
    }

    override fun attack(blockState: BlockState, level: Level, blockPos: BlockPos, player: Player) {
        val itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND)
        val blockEntity = level.getBlockEntity(blockPos)
        if (blockEntity is DisplayPedestalBlockEntity) {
            val peripheral = blockEntity.getPeripheral(Direction.EAST)
            if (peripheral != null) {
                peripheral.queueEvent("pedestal_left_click", LuaRepresentation.forItemStack(itemInHand, RepresentationMode.FULL))
            }
        }
        super.attack(blockState, level, blockPos, player)
    }

    override fun newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return DisplayPedestalBlockEntity(blockPos, blockState)
    }
}
