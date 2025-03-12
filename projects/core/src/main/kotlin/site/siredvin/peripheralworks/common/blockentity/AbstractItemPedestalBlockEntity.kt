package site.siredvin.peripheralworks.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.broccolium.modules.storage.item.ContainerWrapper
import site.siredvin.peripheralworks.api.IItemStackStorage
import site.siredvin.tweakium.modules.peripheral.api.IOwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.blockentity.MutablePeripheralBlockEntity
import java.util.function.Predicate

abstract class AbstractItemPedestalBlockEntity<T : IOwnedPeripheral<*>>(blockEntityType: BlockEntityType<*>, blockPos: BlockPos, blockState: BlockState) :
    MutablePeripheralBlockEntity<T>(
        blockEntityType,
        blockPos,
        blockState,
    ),
    IItemStackStorage,
    Container {

    companion object {
        private const val STORED_ITEM_STACK_TAG = "storedItemStack"
    }

    abstract val itemFilter: Predicate<ItemStack>

    protected class ExtraSimpleStorage<T : IOwnedPeripheral<*>>(private val itemPedestalBlockEntity: AbstractItemPedestalBlockEntity<T>) : SimpleContainer(1) {
        override fun setChanged() {
            itemPedestalBlockEntity.pushInternalDataChangeToClient()
        }

        override fun canPlaceItem(slot: Int, stack: ItemStack): Boolean = itemPedestalBlockEntity.itemFilter.test(stack)
    }

    protected val inventory = ExtraSimpleStorage(this)
    override val storage = ContainerWrapper(inventory)

    override val storedStack: ItemStack
        get() {
            return inventory.getItem(0)
        }

    override fun loadInternalData(data: CompoundTag, state: BlockState?): BlockState {
        if (data.contains(STORED_ITEM_STACK_TAG)) {
            val itemList = data.getList(STORED_ITEM_STACK_TAG, 10)
            if (itemList.isEmpty()) {
                inventory.clearContent()
            } else {
                inventory.fromTag(itemList)
            }
        }
        return state ?: blockState
    }

    override fun saveInternalData(data: CompoundTag): CompoundTag {
        data.put(STORED_ITEM_STACK_TAG, inventory.createTag())
        return data
    }

    override fun clearContent() {
        inventory.clearContent()
    }

    override fun getContainerSize(): Int = inventory.containerSize

    override fun isEmpty(): Boolean = inventory.isEmpty

    override fun getItem(p0: Int): ItemStack = inventory.getItem(p0)

    override fun removeItem(p0: Int, p1: Int): ItemStack = inventory.removeItem(p0, p1)

    override fun removeItemNoUpdate(p0: Int): ItemStack = inventory.removeItemNoUpdate(p0)

    override fun setItem(p0: Int, p1: ItemStack) {
        inventory.setItem(p0, p1)
    }

    override fun stillValid(p0: Player): Boolean = inventory.stillValid(p0)
}
