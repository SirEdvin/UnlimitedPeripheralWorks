package site.siredvin.peripheralworks.fabric

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralworks.api.ISavableComponent

class FabricCustomSlottedStorage(slots: Int, slotScale: Int, trigger: Runnable, capacity: Int? = null, accepts: (ItemStack) -> Boolean = { true }) :
    CombinedSlottedStorage<ItemVariant, SingleVariantStorage<ItemVariant>>(createSlots(slots, slotScale, trigger, capacity, accepts)),
    ISavableComponent {
    class SavedSingleItemVariant(private val slotScale: Int, private val trigger: Runnable, private val limit: Int?, private val accepts: (ItemStack) -> Boolean) : SingleVariantStorage<ItemVariant>() {
        override fun getBlankVariant(): ItemVariant = ItemVariant.blank()

        override fun canInsert(variant: ItemVariant): Boolean = accepts(variant.toStack())

        override fun getCapacity(variant: ItemVariant): Long {
            if (limit != null) return limit.toLong()
            if (variant.isBlank) {
                return 64 * slotScale.toLong()
            }
            return (variant.item.defaultMaxStackSize * slotScale).toLong()
        }

        override fun onFinalCommit() {
            super.onFinalCommit()
            trigger.run()
        }
    }
    companion object {
        private fun createSlots(count: Int, slotScale: Int, trigger: Runnable, capacity: Int?, accepts: (ItemStack) -> Boolean): List<SingleVariantStorage<ItemVariant>> {
            val slots = mutableListOf<SingleVariantStorage<ItemVariant>>()
            for (i in 0..<count) {
                slots.add(SavedSingleItemVariant(slotScale, trigger, capacity, accepts))
            }
            return slots
        }
    }

    fun replace(slot: Int, expected: ItemStack, replacement: ItemStack): Boolean {
        val target = parts[slot]
        if (!ItemStack.matches(target.resource.toStack(target.amount.toInt()), expected)) return false
        Transaction.openOuter().use { transaction ->
            if (target.extract(ItemVariant.of(expected), expected.count.toLong(), transaction) != expected.count.toLong()) return false
            if (target.insert(ItemVariant.of(replacement), replacement.count.toLong(), transaction) != replacement.count.toLong()) return false
            transaction.commit()
        }
        return true
    }

    override fun save(): Tag {
        val list = ListTag()
        for (slot in slots) {
            val tag = CompoundTag()
            tag.put("Variant", ItemVariant.CODEC.encodeStart(NbtOps.INSTANCE, slot.resource).getOrThrow())
            tag.putLong("Amount", slot.amount)
            list.add(tag)
        }
        return list
    }

    override fun load(tag: Tag) {
        val list = tag as ListTag
        for (i in 0 until list.size) {
            val innerTag = list.get(i) as CompoundTag
            parts.get(i).variant = ItemVariant.CODEC.parse(NbtOps.INSTANCE, innerTag.getCompound("Variant")).getOrThrow()
            parts.get(i).amount = innerTag.getLong("Amount")
        }
    }
}
