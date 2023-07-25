package site.siredvin.peripheralworks.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralium.common.blockentities.MutableNBTBlockEntity
import site.siredvin.peripheralium.common.blocks.FacingBlockEntityBlock
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.block.EntityLink
import site.siredvin.peripheralworks.common.item.EntityCard
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.computercraft.peripherals.EntityLinkPeripheral

class EntityLinkBlockEntity(blockPos: BlockPos, blockState: BlockState) :
    MutableNBTBlockEntity<EntityLinkPeripheral>(BlockEntityTypes.ENTITY_LINK.get(), blockPos, blockState) {
    companion object {
        const val STORED_CARD_TAG = "storedCard"
    }

    private var _storedStack = ItemStack.EMPTY
    private var _entity: Entity? = null

    var storedStack: ItemStack
        get() = _storedStack
        set(value) {
            if (value.isEmpty) {
                _storedStack = ItemStack.EMPTY
                refreshEntity()
                pushInternalDataChangeToClient(blockState.setValue(EntityLink.CONFIGURED, false).setValue(EntityLink.ENTITY_FOUND, _entity != null))
            } else if (value.`is`(Items.ENTITY_CARD.get())) {
                _storedStack = value
                refreshEntity()
                pushInternalDataChangeToClient(blockState.setValue(EntityLink.CONFIGURED, true).setValue(EntityLink.ENTITY_FOUND, _entity != null))
            } else {
                PeripheralWorksCore.LOGGER.warn("Something trying to set item that is not entity card to entity link!")
            }
        }

    val entity: Entity?
        get() = _entity

    val facing: Direction
        get() = blockState.getValue(FacingBlockEntityBlock.FACING)

    override fun createPeripheral(side: Direction): EntityLinkPeripheral {
        return EntityLinkPeripheral(this)
    }

    private fun isSameEntity(first: Entity?, second: Entity?): Boolean {
        if (first == null) {
            return second == null
        }
        if (second == null) return false
        return first.uuid == second.uuid
    }

    private fun updateEntity(newEntity: Entity?) {
        if (!isSameEntity(_entity, newEntity)) {
            val isNull = newEntity == null || newEntity.isRemoved
            _entity = if (isNull) {
                null
            } else {
                newEntity
            }
            // Reset peripheral
            peripheral = null
            // Validate entity found block property
            if (blockState.getValue(EntityLink.CONFIGURED)) {
                val realEntityState = blockState.getValue(EntityLink.ENTITY_FOUND)
                if (realEntityState != (_entity != null)) {
                    pushInternalDataChangeToClient(blockState.setValue(EntityLink.ENTITY_FOUND, _entity != null))
                }
            }
        }
    }

    private fun refreshEntity() {
        if (_entity?.isRemoved == true) {
            updateEntity(null)
        } else {
            if (!_storedStack.isEmpty && !EntityCard.isEmpty(_storedStack)) {
                val serverLevel = level as? ServerLevel ?: return
                val entityUUID = EntityCard.getEntityUUID(_storedStack)
                if (entityUUID == null) {
                    updateEntity(null)
                } else {
                    val newEntity = serverLevel.getEntity(entityUUID) ?: return
                    updateEntity(newEntity)
                }
            } else if (_entity != null) {
                updateEntity(null)
            }
        }
    }

    override fun handleTick(level: Level, pos: BlockPos, state: BlockState) {
        if (peripheral != null && !peripheral!!.configured) {
            peripheral!!.recollectPlugins()
        }
        refreshEntity()
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
