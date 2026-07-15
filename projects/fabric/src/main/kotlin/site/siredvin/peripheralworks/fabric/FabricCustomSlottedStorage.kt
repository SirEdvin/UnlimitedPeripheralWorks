package site.siredvin.peripheralworks.fabric

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import site.siredvin.peripheralworks.api.ISavableComponent

class FabricCustomSlottedStorage(slots: Int, slotScale: Int, trigger: Runnable) :
    CombinedSlottedStorage<ItemVariant, SingleVariantStorage<ItemVariant>>(createSlots(slots, slotScale, trigger)),
    ISavableComponent {
    class SavedSingleItemVariant(private val slotScale: Int, private val trigger: Runnable) : SingleVariantStorage<ItemVariant>() {
        override fun getBlankVariant(): ItemVariant = ItemVariant.blank()

        override fun getCapacity(variant: ItemVariant): Long {
            if (variant.isBlank) {
                return 64 * slotScale.toLong()
            }
            return (variant.toStack().maxStackSize * slotScale).toLong()
        }

        override fun onFinalCommit() {
            super.onFinalCommit()
            trigger.run()
        }
    }
    companion object {
        private fun createSlots(count: Int, slotScale: Int, trigger: Runnable): List<SingleVariantStorage<ItemVariant>> {
            val slots = mutableListOf<SingleVariantStorage<ItemVariant>>()
            for (i in 0..<count) {
                slots.add(SavedSingleItemVariant(slotScale, trigger))
            }
            return slots
        }
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
            parts.get(i).variant = ItemVariant.CODEC.parse(NbtOps.INSTANCE, innerTag.get("Variant")).getOrThrow()
            parts.get(i).amount = innerTag.getLong("Amount")
        }
    }
}
