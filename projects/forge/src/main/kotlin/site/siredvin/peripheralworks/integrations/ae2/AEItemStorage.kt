package site.siredvin.peripheralworks.integrations.ae2

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEItemKey
import appeng.api.storage.MEStorage
import appeng.blockentity.grid.AENetworkBlockEntity
import net.minecraft.world.item.ItemStack
import site.siredvin.broccolium.modules.storage.base.api.AgnosticStorage
import site.siredvin.broccolium.modules.storage.base.api.SomethingOperator
import site.siredvin.broccolium.modules.storage.item.ItemStorageUtils
import java.util.function.Predicate

class AEItemStorage(private val storage: MEStorage, private val entity: AENetworkBlockEntity) : AgnosticStorage<ItemStack, Int> {
    override fun getContent(): Iterator<ItemStack> {
        return storage.availableStacks.mapNotNull {
            if (it.key !is AEItemKey) return@mapNotNull null
            return@mapNotNull (it.key as AEItemKey).toStack(it.longValue.toInt())
        }.iterator()
    }

    override val maxStackSize: Int
        get() = Int.MAX_VALUE
    override val operator: SomethingOperator<ItemStack, Int>
        get() = ItemStorageUtils

    override fun setChanged() {
        entity.setChanged()
    }

    override fun store(stack: ItemStack, simulate: Boolean): ItemStack {
        val insertedAmount = storage.insert(AEItemKey.of(stack), stack.count.toLong(), if (simulate) Actionable.SIMULATE else Actionable.MODULATE, IActionSource.ofMachine(entity))
        if (insertedAmount == 0L) return stack
        stack.shrink(insertedAmount.toInt())
        return stack
    }

    override fun take(predicate: Predicate<ItemStack>, limit: Int, simulate: Boolean): ItemStack {
        val itemToTransfer = storage.availableStacks.find {
            val aeKey = it.key
            if (aeKey !is AEItemKey) {
                return@find false
            }
            return@find predicate.test(aeKey.toStack(it.longValue.toInt()))
        } ?: return ItemStack.EMPTY
        val extractedAmount = storage.extract(itemToTransfer.key, minOf(limit.toLong(), itemToTransfer.longValue), if (simulate) Actionable.SIMULATE else Actionable.MODULATE, IActionSource.ofMachine(entity))
        if (extractedAmount == 0L) return ItemStack.EMPTY
        return (itemToTransfer.key as AEItemKey).toStack(extractedAmount.toInt())
    }
}
