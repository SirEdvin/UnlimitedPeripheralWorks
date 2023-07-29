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
import site.siredvin.peripheralium.computercraft.peripheral.ability.PeripheralOwnerAbility
import site.siredvin.peripheralium.computercraft.peripheral.ability.ScanningAbility
import site.siredvin.peripheralium.computercraft.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.peripheralium.computercraft.peripheral.owner.EntityProxyPeripheralOwner
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.block.EntityLink
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.common.item.EntityCard
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.common.setup.Blocks
import site.siredvin.peripheralworks.common.setup.Items
import site.siredvin.peripheralworks.computercraft.operations.SphereOperations
import site.siredvin.peripheralworks.computercraft.peripherals.EntityLinkPeripheral

class EntityLinkBlockEntity(blockPos: BlockPos, blockState: BlockState) :
    MutableNBTBlockEntity<EntityLinkPeripheral>(BlockEntityTypes.ENTITY_LINK.get(), blockPos, blockState) {
    companion object {
        const val STORED_CARD_TAG = "storedCard"
        const val UPGRADES_TAG = "upgrades"
    }

    data class Upgrades(var scanner: Boolean = false) {
        companion object {
            const val SCANNER_TAG = "scanner"
        }
        fun save(): CompoundTag {
            val tag = CompoundTag()
            tag.putBoolean(SCANNER_TAG, scanner)
            return tag
        }

        fun load(tag: CompoundTag) {
            if (tag.contains(SCANNER_TAG)) {
                scanner = tag.getBoolean(SCANNER_TAG)
            }
        }
    }

    private var _storedStack = ItemStack.EMPTY
    private var _entity: Entity? = null
    val upgrades: Upgrades = Upgrades()

    var storedStack: ItemStack
        get() = _storedStack
        set(value) {
            if (value.isEmpty) {
                _storedStack = ItemStack.EMPTY
                refreshEntity()
                pushInternalDataChangeToClient(blockState.setValue(EntityLink.CONFIGURED, false))
            } else if (value.`is`(Items.ENTITY_CARD.get())) {
                _storedStack = value
                refreshEntity()
                pushInternalDataChangeToClient(blockState.setValue(EntityLink.CONFIGURED, true))
            } else {
                PeripheralWorksCore.LOGGER.warn("Something trying to set item that is not entity card to entity link!")
            }
        }

    val entity: Entity?
        get() {
            if (!_storedStack.isEmpty && !EntityCard.isEmpty(_storedStack) && _entity == null) {
                refreshEntity()
            }
            return _entity
        }

    val facing: Direction
        get() = blockState.getValue(FacingBlockEntityBlock.FACING)

    override fun createPeripheral(side: Direction): EntityLinkPeripheral {
        if (_entity == null) return EntityLinkPeripheral(this, BlockEntityPeripheralOwner(this))
        val owner = EntityProxyPeripheralOwner(this, _entity!!)

        if (upgrades.scanner) {
            owner.attachOperations(config = PeripheralWorksConfig)
            val operation = SphereOperations.PORTABLE_UNIVERSAL_SCAN
            val maxRadius = operation.maxFreeRadius
            owner.attachAbility(
                PeripheralOwnerAbility.SCANNING,
                ScanningAbility(owner, maxRadius).attachBlockScan(
                    operation,
                ).attachLivingEntityScan(
                    operation,
                    { true },
                ).attachItemScan(
                    operation,
                ).attachPlayerScan(
                    operation,
                ),
            )
        }
        return EntityLinkPeripheral(this, owner)
    }

    private fun isSameEntity(first: Entity?, second: Entity?): Boolean {
        if (first == null) {
            return second == null
        }
        if (second == null) return false
        return first.uuid == second.uuid
    }

    private fun resetPeripheral() {
        peripheral = null
        pushInternalDataChangeToClient(blockState.setValue(EntityLink.ENTITY_TRIGGER, (blockState.getValue(EntityLink.ENTITY_TRIGGER) + 1) % 4))
    }

    private fun updateEntity(newEntity: Entity?) {
        if (!isSameEntity(_entity, newEntity)) {
            val isNull = newEntity == null || newEntity.isRemoved
            _entity = if (isNull) {
                null
            } else {
                newEntity
            }
            resetPeripheral()
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
            peripheral!!.configure()
        }
        refreshEntity()
        super.handleTick(level, pos, state)
    }

    fun isSuitableUpgrade(stack: ItemStack): Boolean {
        return stack.`is`(Blocks.UNIVERSAL_SCANNER.get().asItem()) && !upgrades.scanner
    }

    fun injectUpgrade(stack: ItemStack): Boolean {
        if (stack.`is`(Blocks.UNIVERSAL_SCANNER.get().asItem()) && !upgrades.scanner) {
            upgrades.scanner = true
            resetPeripheral()
        }
        return false
    }

    fun ejectUpgrade(): ItemStack {
        if (!upgrades.scanner) return ItemStack.EMPTY
        upgrades.scanner = false
        resetPeripheral()
        return Blocks.UNIVERSAL_SCANNER.get().asItem().defaultInstance
    }

    fun collectUpgrades(): List<ItemStack> {
        if (upgrades.scanner) return listOf(Blocks.UNIVERSAL_SCANNER.get().asItem().defaultInstance)
        return emptyList()
    }

    override fun loadInternalData(data: CompoundTag, state: BlockState?): BlockState {
        var resultState = state ?: blockState
        if (data.contains(STORED_CARD_TAG)) {
            _storedStack = ItemStack.of(data.getCompound(STORED_CARD_TAG))
            resultState = resultState.setValue(EntityLink.CONFIGURED, true)
        } else {
            resultState = resultState.setValue(EntityLink.CONFIGURED, false)
        }
        if (data.contains(UPGRADES_TAG)) upgrades.load(data.getCompound(UPGRADES_TAG))
        return resultState
    }

    override fun saveInternalData(data: CompoundTag): CompoundTag {
        if (!_storedStack.isEmpty) {
            data.put(STORED_CARD_TAG, _storedStack.save(CompoundTag()))
        }
        data.put(UPGRADES_TAG, upgrades.save())
        return data
    }
}
