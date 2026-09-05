package site.siredvin.peripheralworks.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.broccolium.modules.storage.base.api.SlottedAgnosticStorage
import site.siredvin.peripheralworks.api.IItemStackStorage
import site.siredvin.peripheralworks.api.IPlatformItemStorageHolder
import site.siredvin.peripheralworks.api.ISavableComponent
import site.siredvin.peripheralworks.xplat.ModPlatform
import site.siredvin.tweakium.modules.peripheral.api.IOwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.blockentity.MutablePeripheralBlockEntity
import java.util.function.Predicate

abstract class AbstractItemPedestalBlockEntity<T : IOwnedPeripheral<*>>(blockEntityType: BlockEntityType<*>, blockPos: BlockPos, blockState: BlockState, val holdingStacks: Int = 1, capacity: Int? = null, accepts: (ItemStack) -> Boolean = { true }) :
    MutablePeripheralBlockEntity<T>(
        blockEntityType,
        blockPos,
        blockState,
    ),
    IItemStackStorage,
    IPlatformItemStorageHolder {

    companion object {
        private const val LEGACY_STORED_ITEM_STACK_TAG = "storedItemStack"
        private const val STORED_ITEM_STACK_TAG = "storedItemStackV2"
    }

    abstract val itemFilter: Predicate<ItemStack>

    protected val inventory: ISavableComponent
    final override val storage: SlottedAgnosticStorage<ItemStack, Int>

    init {
        val pair = ModPlatform.baseInnerPlatform.createSlottedItemStorage(1, holdingStacks, {
            this.pushInternalDataChangeToClient()
        }, capacity, accepts)
        inventory = pair.first
        storage = pair.second
    }

    override val storedStack: ItemStack
        get() {
            return storage.get(0)
        }

    override fun getPlatformItemStorage(): Any = inventory

    protected fun replaceStoredStack(expected: ItemStack, replacement: ItemStack): Boolean = ModPlatform.baseInnerPlatform.replaceSlottedItem(inventory, 0, expected, replacement)

    override fun loadInternalData(data: CompoundTag, state: BlockState?): BlockState {
        if (data.contains(LEGACY_STORED_ITEM_STACK_TAG)) {
            val tag = data.get(LEGACY_STORED_ITEM_STACK_TAG)
            if (tag is CompoundTag) {
                inventory.load(tag)
            } else if (tag is ListTag) {
                tag.forEach {
                    if (it is CompoundTag) {
                        val stack = ItemStack.of(it)
                        storage.store(stack, false)
                    }
                }
            }
        }
        if (data.contains(STORED_ITEM_STACK_TAG)) {
            inventory.load(data.get(STORED_ITEM_STACK_TAG)!!)
        }
        return state ?: blockState
    }

    override fun saveInternalData(data: CompoundTag): CompoundTag {
        data.put(STORED_ITEM_STACK_TAG, inventory.save())
        return data
    }
}
