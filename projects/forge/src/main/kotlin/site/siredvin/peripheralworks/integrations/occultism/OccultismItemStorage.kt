package site.siredvin.peripheralworks.integrations.occultism

import com.github.klikli_dev.occultism.api.common.blockentity.IStorageController
import net.minecraft.world.item.ItemStack
import site.siredvin.peripheralium.api.storage.Storage
import java.util.function.Predicate

class OccultismItemStorage(private val storageController: IStorageController): Storage {
    override fun getItems(): Iterator<ItemStack> {
        return storageController.stacks.iterator()
    }

    override fun setChanged() {
        storageController.onContentsChanged()
    }

    override fun storeItem(stack: ItemStack): ItemStack {
        val returnedAmount = storageController.insertStack(stack, false)
        return stack.copyWithCount(returnedAmount)
    }

    override fun takeItems(predicate: Predicate<ItemStack>, limit: Int): ItemStack {
        return storageController.getItemStack(predicate, limit, false)
    }
}