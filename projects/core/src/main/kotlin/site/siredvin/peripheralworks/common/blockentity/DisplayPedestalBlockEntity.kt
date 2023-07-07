package site.siredvin.peripheralworks.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralium.common.blockentities.MutableNBTBlockEntity
import site.siredvin.peripheralworks.api.IItemStackHolder
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.computercraft.peripherals.DisplayPedestalPeripheral

class DisplayPedestalBlockEntity(blockPos: BlockPos, blockState: BlockState) :
    MutableNBTBlockEntity<DisplayPedestalPeripheral>(
        BlockEntityTypes.DISPLAY_PEDESTAL.get(),
        blockPos,
        blockState,
    ),
    IItemStackHolder {

    companion object {
        private const val STORED_ITEM_STACK_TAG = "storedItemStack"
        private const val RENDER_LABEL_TAG = "renderLabel"
        private const val RENDER_ITEM_TAG = "renderItem"
    }
    private var _storedStack: ItemStack = ItemStack.EMPTY
    private var _renderLabel: Boolean = true
    private var _renderItem: Boolean = true

    override var storedStack: ItemStack
        get() {
            return _storedStack
        }
        set(value) {
            _storedStack = value
            pushInternalDataChangeToClient()
        }

    override var renderItem: Boolean
        get() = _renderItem
        set(value) {
            _renderItem = value
            pushInternalDataChangeToClient()
        }

    override var renderLabel: Boolean
        get() = _renderLabel
        set(value) {
            _renderLabel = value
            pushInternalDataChangeToClient()
        }

    override fun loadInternalData(data: CompoundTag, state: BlockState?): BlockState {
        if (data.contains(STORED_ITEM_STACK_TAG)) {
            _storedStack = ItemStack.of(data.getCompound(STORED_ITEM_STACK_TAG))
        }
        if (data.contains(RENDER_LABEL_TAG)) {
            _renderLabel = data.getBoolean(RENDER_LABEL_TAG)
        }
        if (data.contains(RENDER_ITEM_TAG)) {
            _renderItem = data.getBoolean(RENDER_ITEM_TAG)
        }
        return state ?: blockState
    }

    override fun saveInternalData(data: CompoundTag): CompoundTag {
        data.put(STORED_ITEM_STACK_TAG, _storedStack.save(CompoundTag()))
        data.putBoolean(RENDER_ITEM_TAG, _renderItem)
        data.putBoolean(RENDER_LABEL_TAG, _renderLabel)
        return data
    }

    override fun createPeripheral(side: Direction): DisplayPedestalPeripheral {
        return DisplayPedestalPeripheral(this)
    }
}
