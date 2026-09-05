package site.siredvin.peripheralworks.forge

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.ItemStackHandler
import site.siredvin.peripheralworks.api.ISavableComponent

class ForgeCustomSlottedStorage(size: Int, private val slotScale: Int, private val trigger: Runnable, private val capacity: Int? = null, private val accepts: (ItemStack) -> Boolean = { true }) :
    ItemStackHandler(size),
    ISavableComponent {

    override fun getSlotLimit(slot: Int): Int = capacity ?: (64 * slotScale)

    override fun isItemValid(slot: Int, stack: ItemStack): Boolean = accepts(stack)

    fun replace(slot: Int, expected: ItemStack, replacement: ItemStack): Boolean {
        if (!ItemStack.matches(getStackInSlot(slot), expected)) return false
        if (!isItemValid(slot, replacement) || replacement.count > getStackLimit(slot, replacement)) return false
        setStackInSlot(slot, replacement.copy())
        return true
    }

    override fun getStackLimit(slot: Int, stack: ItemStack): Int {
        if (capacity != null) return capacity
        if (stack.isEmpty) {
            return 64 * slotScale
        }
        return stack.maxStackSize * slotScale
    }

    override fun onContentsChanged(slot: Int) {
        trigger.run()
    }

    override fun save(): Tag {
        val nbtTagList = ListTag()
        for (i in stacks.indices) {
            if (!stacks[i].isEmpty) {
                val itemTag = CompoundTag()
                itemTag.putInt("Slot", i)
                stacks[i].save(itemTag)
                itemTag.putInt("Count", stacks[i].count)
                nbtTagList.add(itemTag)
            }
        }
        val nbt = CompoundTag()
        nbt.put("Items", nbtTagList)
        nbt.putInt("Size", stacks.size)
        return nbt
    }

    override fun load(tag: Tag) {
        val nbt = tag as? CompoundTag ?: return
        setSize(if (nbt.contains("Size", Tag.TAG_INT.toInt())) nbt.getInt("Size") else stacks.size)
        val tagList: ListTag = nbt.getList("Items", Tag.TAG_COMPOUND.toInt())
        for (i in tagList.indices) {
            val itemTags = tagList.getCompound(i)
            val slot = itemTags.getInt("Slot")

            if (slot >= 0 && slot < stacks.size) {
                val baseStack = ItemStack.of(itemTags)
                baseStack.count = itemTags.getInt("Count")
                stacks.set(slot, baseStack)
            }
        }
        onLoad()
    }
}
