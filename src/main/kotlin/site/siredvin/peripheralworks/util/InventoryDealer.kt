package site.siredvin.peripheralworks.util

import dan200.computercraft.shared.util.InventoryUtil
import dan200.computercraft.shared.util.ItemStorage
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity

object InventoryDealer {
    fun moveItem(from: ItemStorage, fromSlot: Int, to: ItemStorage, toSlot: Int, limit: Int): Int {
        // Moving nothing is easy
        if (limit == 0) {
            return 0
        }

        // Get stack to move
        val stack = InventoryUtil.takeItems(limit, from, fromSlot, 1, fromSlot)
        if (stack.isEmpty) {
            return 0
        }
        val stackCount = stack.count

        // Move items in
        val remainder: ItemStack = if (toSlot < 0) {
            InventoryUtil.storeItems(stack, to)
        } else {
            InventoryUtil.storeItems(stack, to, toSlot, 1, toSlot)
        }

        // Calculate items moved
        val count = stackCount - remainder.count
        if (!remainder.isEmpty) {
            // Put the remainder back
            InventoryUtil.storeItems(remainder, from, fromSlot, 1, fromSlot)
        }
        return count
    }
}