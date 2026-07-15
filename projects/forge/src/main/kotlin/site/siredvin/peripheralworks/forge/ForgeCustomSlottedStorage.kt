package site.siredvin.peripheralworks.forge

import com.mojang.serialization.Dynamic
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.util.datafix.DataFixers
import net.minecraft.util.datafix.fixes.References
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.ItemStackHandler
import net.neoforged.neoforge.server.ServerLifecycleHooks
import site.siredvin.peripheralworks.api.ISavableComponent

class ForgeCustomSlottedStorage(size: Int, private val slotScale: Int, private val trigger: Runnable) :
    ItemStackHandler(size),
    ISavableComponent {

    override fun getSlotLimit(slot: Int): Int = 64 * slotScale

    override fun getStackLimit(slot: Int, stack: ItemStack): Int {
        if (stack.isEmpty) {
            return 64 * slotScale
        }
        return stack.maxStackSize * slotScale
    }

    override fun onContentsChanged(slot: Int) {
        trigger.run()
    }

    override fun save(): Tag {
        val registries = registryAccess()
        val nbtTagList = ListTag()
        for (i in stacks.indices) {
            if (!stacks[i].isEmpty) {
                val itemTag = stacks[i].copyWithCount(1).save(registries, CompoundTag()) as CompoundTag
                itemTag.putInt("Slot", i)
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
        val registries = registryAccess()
        val nbt = tag as? CompoundTag ?: return
        setSize(if (nbt.contains("Size", Tag.TAG_INT.toInt())) nbt.getInt("Size") else stacks.size)
        val tagList: ListTag = nbt.getList("Items", Tag.TAG_COMPOUND.toInt())
        for (i in tagList.indices) {
            val itemTags = tagList.getCompound(i)
            val slot = itemTags.getInt("Slot")

            if (slot >= 0 && slot < stacks.size) {
                val stackTag = if (itemTags.contains("Count") && !itemTags.contains("count")) {
                    DataFixers.getDataFixer()
                        .update(References.ITEM_STACK, Dynamic(NbtOps.INSTANCE, itemTags), LEGACY_DATA_VERSION, CURRENT_DATA_VERSION)
                        .value as CompoundTag
                } else {
                    itemTags
                }
                val baseStack = ItemStack.parseOptional(registries, stackTag)
                baseStack.count = itemTags.getInt("Count")
                stacks.set(slot, baseStack)
            }
        }
        onLoad()
    }

    private fun registryAccess(): RegistryAccess = ServerLifecycleHooks.getCurrentServer()?.registryAccess()
        ?: RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)

    companion object {
        private const val LEGACY_DATA_VERSION = 3465
        private const val CURRENT_DATA_VERSION = 3955
    }
}
