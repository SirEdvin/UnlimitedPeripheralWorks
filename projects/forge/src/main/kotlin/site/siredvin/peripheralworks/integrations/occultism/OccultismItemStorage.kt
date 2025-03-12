package site.siredvin.peripheralworks.integrations.occultism

import com.klikli_dev.occultism.api.common.blockentity.IStorageController
import net.minecraft.world.item.ItemStack
import site.siredvin.broccolium.modules.storage.item.api.AgnosticItemStorage
import java.util.function.Predicate

class OccultismItemStorage(private val storageController: IStorageController) : AgnosticItemStorage {
    override fun getItems(): Iterator<ItemStack> = storageController.stacks.iterator()

    override fun setChanged() {
        storageController.onContentsChanged()
    }

    override fun storeItem(stack: ItemStack): ItemStack {
        val returnedAmount = storageController.insertStack(stack, false)
        return stack.copyWithCount(returnedAmount)
    }

    override fun takeItems(predicate: Predicate<ItemStack>, limit: Int): ItemStack = storageController.getItemStack(predicate, limit, false)
}
