package site.siredvin.peripheralworks.common.block

import net.minecraft.core.BlockPos
import net.minecraft.world.Containers
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import site.siredvin.peripheralium.storages.item.ItemStorageUtils
import site.siredvin.peripheralium.util.BlockUtil
import site.siredvin.peripheralworks.api.IItemStackStorage

abstract class AbstractItemPedestal<T : BlockEntity> : BasePedestal<T>(BlockUtil.defaultProperties()) {

    @Deprecated("Deprecated in Java")
    override fun use(
        blockState: BlockState,
        level: Level,
        blockPos: BlockPos,
        player: Player,
        interactionHand: InteractionHand,
        blockHitResult: BlockHitResult,
    ): InteractionResult {
        val itemInHand = player.getItemInHand(interactionHand)
        if (!itemInHand.isEmpty) {
            val blockEntity = level.getBlockEntity(blockPos)
            if (blockEntity is IItemStackStorage) {
                if (blockEntity.storedStack.isEmpty) {
                    val storeResult = blockEntity.storage.storeItem(itemInHand)
                    if (storeResult.isEmpty) {
                        player.setItemInHand(interactionHand, ItemStack.EMPTY)
                        return InteractionResult.CONSUME
                    }
                }
            }
        }
        @Suppress("DEPRECATION")
        return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult)
    }

    @Deprecated("Deprecated in Java")
    override fun onRemove(blockState: BlockState, level: Level, blockPos: BlockPos, replace: BlockState, bl: Boolean) {
        if (blockState.block !== replace.block) {
            val blockEntity = level.getBlockEntity(blockPos)
            if (blockEntity is IItemStackStorage) {
                if (!blockEntity.storedStack.isEmpty) {
                    Containers.dropItemStack(
                        level,
                        blockPos.x.toDouble(),
                        blockPos.y.toDouble(),
                        blockPos.z.toDouble(),
                        blockEntity.storedStack,
                    )
                }
            }
        }
        @Suppress("DEPRECATION")
        super.onRemove(blockState, level, blockPos, replace, bl)
    }

    @Deprecated("Deprecated in Java")
    override fun attack(blockState: BlockState, level: Level, blockPos: BlockPos, player: Player) {
        val itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND)
        if (itemInHand.isEmpty) {
            val blockEntity = level.getBlockEntity(blockPos)
            if (blockEntity is IItemStackStorage) {
                if (!blockEntity.storedStack.isEmpty) {
                    val storedStack = blockEntity.storage.takeItems(ItemStorageUtils.ALWAYS, Int.MAX_VALUE)
                    if (!storedStack.isEmpty) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, storedStack)
                    }
                }
            }
        }
        @Suppress("DEPRECATION")
        super.attack(blockState, level, blockPos, player)
    }
}
