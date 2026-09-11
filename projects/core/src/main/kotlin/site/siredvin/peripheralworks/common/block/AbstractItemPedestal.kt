package site.siredvin.peripheralworks.common.block

import net.minecraft.core.BlockPos
import net.minecraft.world.Containers
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import site.siredvin.broccolium.modules.base.util.BlockUtil
import site.siredvin.broccolium.modules.storage.item.ItemStorageUtils
import site.siredvin.peripheralworks.api.IItemStackStorage
import site.siredvin.peripheralworks.common.blockentity.AbstractItemPedestalBlockEntity
import kotlin.math.min

abstract class AbstractItemPedestal<T : BlockEntity>(properties: Properties = BlockUtil.defaultProperties()) : BasePedestal<T>(properties) {

    override fun useItemOn(
        itemStack: ItemStack,
        blockState: BlockState,
        level: Level,
        blockPos: BlockPos,
        player: Player,
        interactionHand: InteractionHand,
        blockHitResult: BlockHitResult,
    ): ItemInteractionResult {
        val itemInHand = player.getItemInHand(interactionHand)
        val blockEntity = level.getBlockEntity(blockPos)
        if (interactionHand == InteractionHand.MAIN_HAND) {
            if (blockEntity is AbstractItemPedestalBlockEntity<*>) {
                if (!itemInHand.isEmpty) {
                    // Fabric storage mutates its argument; retain the hand stack for comparison and synchronization.
                    val leftover = blockEntity.storage.store(itemInHand.copy(), false)
                    if (!ItemStack.matches(leftover, itemInHand)) {
                        player.setItemInHand(interactionHand, leftover)
                        return ItemInteractionResult.CONSUME
                    }
                }
            }
        }
        return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult)
    }

    @Deprecated("Deprecated in Java")
    override fun onRemove(blockState: BlockState, level: Level, blockPos: BlockPos, replace: BlockState, bl: Boolean) {
        if (blockState.block !== replace.block) {
            val blockEntity = level.getBlockEntity(blockPos)
            if (blockEntity is IItemStackStorage) {
                if (!blockEntity.storedStack.isEmpty) {
                    var slidingCount = blockEntity.storedStack.count
                    while (slidingCount > 0) {
                        val droppedCount = min(blockEntity.storedStack.maxStackSize, slidingCount)
                        Containers.dropItemStack(
                            level,
                            blockPos.x.toDouble(),
                            blockPos.y.toDouble(),
                            blockPos.z.toDouble(),
                            blockEntity.storedStack.copyWithCount(droppedCount),
                        )
                        slidingCount -= droppedCount
                    }
                }
            }
        }
        @Suppress("DEPRECATION")
        super.onRemove(blockState, level, blockPos, replace, bl)
    }

    @Deprecated("Deprecated in Java")
    override fun attack(blockState: BlockState, level: Level, blockPos: BlockPos, player: Player) {
        val itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND)
        val blockEntity = level.getBlockEntity(blockPos)
        if (blockEntity is IItemStackStorage) {
            if (!blockEntity.storedStack.isEmpty && (ItemStack.isSameItemSameComponents(blockEntity.storedStack, itemInHand) || itemInHand.isEmpty)) {
                val calculatedLimit = if (player.isCrouching) {
                    blockEntity.storedStack.maxStackSize
                } else {
                    1
                }
                val trueLimit = calculatedLimit.coerceAtMost(blockEntity.storedStack.maxStackSize - itemInHand.count)
                if (trueLimit > 0) {
                    val extractedStack = blockEntity.storage.take(ItemStorageUtils.ALWAYS, trueLimit, false)
                    if (!extractedStack.isEmpty) {
                        if (itemInHand.isEmpty) {
                            player.setItemInHand(InteractionHand.MAIN_HAND, extractedStack)
                        } else {
                            itemInHand.grow(extractedStack.count)
                            player.setItemInHand(InteractionHand.MAIN_HAND, itemInHand)
                        }
                    }
                }
            }
        }
        @Suppress("DEPRECATION")
        super.attack(blockState, level, blockPos, player)
    }
}
