package site.siredvin.peripheralworks.integrations.create

import dan200.computercraft.shared.util.ItemStorage
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler
import net.minecraft.world.item.ItemStack

class CreateCCItemStorage(private val handlers: List<ItemStackHandler>) : ItemStorage {
    private fun fromIndex(index: Int): Pair<ItemStackHandler, Int>? {
        var passedCounter = 0
        for (handler in handlers) {
            if (index < handler.slots + passedCounter) {
                return Pair(handler, index - passedCounter)
            } else {
                passedCounter += handler.slots
            }
        }
        return null
    }
    override fun size(): Int {
        return handlers.sumOf { it.slots }
    }

    override fun getStack(slot: Int): ItemStack {
        val pair = fromIndex(slot) ?: return ItemStack.EMPTY
        return pair.first.getStackInSlot(pair.second)
    }

    private fun getSlotLimit(slot: Int): Int {
        val pair = fromIndex(slot) ?: return ItemStack.EMPTY.maxStackSize
        return pair.first.getSlotLimit(pair.second)
    }

    override fun setStack(slot: Int, stack: ItemStack?) {
        val pair = fromIndex(slot)
        pair?.first?.setStackInSlot(pair.second, stack)
    }

    override fun take(slot: Int, limit: Int, filter: ItemStack, simulate: Boolean): ItemStack {
        var existing: ItemStack = getStack(slot)
        if (existing.isEmpty || !filter.isEmpty && !ItemStorage.areStackable(existing, filter)) {
            return ItemStack.EMPTY
        }

        return if (simulate) {
            existing = existing.copy()
            if (existing.count > limit) {
                existing.count = limit
            }
            existing
        } else if (existing.count < limit) {
            setStack(slot, ItemStack.EMPTY)
            existing
        } else {
            val result = existing.split(limit)
            setStack(slot, existing)
            result
        }
    }

    override fun store(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
        if (stack.isEmpty) {
            return stack
        }

        val existing: ItemStack = getStack(slot)
        return if (existing.isEmpty) {
            val limit = minOf(stack.maxStackSize, getSlotLimit(slot))
            if (limit <= 0) {
                return stack
            }
            if (stack.count < limit) {
                if (!simulate) {
                    setStack(slot, stack)
                }
                ItemStack.EMPTY
            } else {
                val stackCopy = stack.copy()
                val insert = stackCopy.split(limit)
                if (!simulate) {
                    setStack(slot, insert)
                }
                stackCopy
            }
        } else if (ItemStorage.areStackable(stack, existing)) {
            val limit = minOf(existing.maxStackSize, getSlotLimit(slot)) - existing.count
            if (limit <= 0) {
                return stack
            }
            if (stack.count < limit) {
                if (!simulate) {
                    existing.grow(stack.count)
                    setStack(slot, existing)
                }
                ItemStack.EMPTY
            } else {
                val stackCopy = stack.copy()
                stackCopy.shrink(limit)
                if (!simulate) {
                    existing.grow(limit)
                    setStack(slot, existing)
                }
                stackCopy
            }
        } else {
            stack
        }
    }
}
