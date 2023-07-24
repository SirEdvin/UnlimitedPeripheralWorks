package site.siredvin.peripheralworks.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralium.common.blockentities.MutableNBTBlockEntity
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.block.EntityLink
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.computercraft.peripherals.EntityLinkPeripheral

class EntityLinkBlockEntity(blockPos: BlockPos, blockState: BlockState) :
    MutableNBTBlockEntity<EntityLinkPeripheral>(BlockEntityTypes.REALITY_FORGER.get(), blockPos, blockState) {
    companion object {
        const val STORED_CARD_TAG = "storedCard"
    }

    private var _storedStack = ItemStack.EMPTY

    var storedStack: ItemStack
        get() = _storedStack
        set(value) {
            if (value.isEmpty) {
                _storedStack = ItemStack.EMPTY
                pushInternalDataChangeToClient(blockState.setValue(EntityLink.CONFIGURED, false))
            } else if (value.`is`(Items.ENTITY_CARD.get())) {
                _storedStack = value
                pushInternalDataChangeToClient(blockState.setValue(EntityLink.CONFIGURED, true))
            } else {
                PeripheralWorksCore.LOGGER.warn("Something trying to set item that is not entity card to entity link!")
            }
        }

    override fun createPeripheral(side: Direction): EntityLinkPeripheral {
        return EntityLinkPeripheral(this)
    }

    override fun handleTick(level: Level, pos: BlockPos, state: BlockState) {
        // TODO: check if entity is still alive
        super.handleTick(level, pos, state)
    }

    override fun loadInternalData(data: CompoundTag, state: BlockState?): BlockState {
        var resultState = state ?: blockState
        if (data.contains(STORED_CARD_TAG)) {
            _storedStack = ItemStack.of(data.getCompound(STORED_CARD_TAG))
            resultState = resultState.setValue(EntityLink.CONFIGURED, true)
        } else {
            resultState = resultState.setValue(EntityLink.CONFIGURED, false)
        }
        return resultState
    }

    override fun saveInternalData(data: CompoundTag): CompoundTag {
        if (!_storedStack.isEmpty) {
            data.put(STORED_CARD_TAG, _storedStack.save(CompoundTag()))
        }
        return data
    }
}
