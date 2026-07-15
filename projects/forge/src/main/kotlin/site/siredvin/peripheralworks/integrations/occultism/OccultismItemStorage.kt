package site.siredvin.peripheralworks.integrations.occultism

import com.klikli_dev.occultism.api.common.blockentity.IStorageController
import net.minecraft.world.item.ItemStack
import site.siredvin.broccolium.modules.storage.base.api.AgnosticStorage
import site.siredvin.broccolium.modules.storage.base.api.SomethingOperator
import site.siredvin.broccolium.modules.storage.item.ItemStorageUtils
import java.util.function.Predicate

class OccultismItemStorage(private val storageController: IStorageController) : AgnosticStorage<ItemStack, Int> {
    override fun getContent(): Iterator<ItemStack> = storageController.stacks.iterator()

    override val maxStackSize: Int
        get() = Int.MAX_VALUE
    override val operator: SomethingOperator<ItemStack, Int>
        get() = ItemStorageUtils

    override fun setChanged() {
        storageController.onContentsChanged()
    }

    override fun store(stack: ItemStack, simulate: Boolean): ItemStack {
        val returnedAmount = storageController.insertStack(stack, simulate)
        return stack.copyWithCount(returnedAmount)
    }

    override fun take(predicate: Predicate<ItemStack>, limit: Int, simulate: Boolean): ItemStack = storageController.getItemStack(predicate, limit, simulate)
}
