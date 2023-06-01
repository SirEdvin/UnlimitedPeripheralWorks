package site.siredvin.peripheralworks.common.blockentity

import dan200.computercraft.api.peripheral.IPeripheral
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.Tag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralium.api.blockentities.IObservingBlockEntity
import site.siredvin.peripheralium.common.blockentities.MutableNBTBlockEntity
import site.siredvin.peripheralium.xplat.PeripheraliumPlatform
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.common.events.BlockStateUpdateEventBus
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheralProxyPeripheral

class PeripheralProxyBlockEntity(blockPos: BlockPos, blockState: BlockState) :
    MutableNBTBlockEntity<PeripheralProxyPeripheral>(BlockEntityTypes.PERIPHERAL_PROXY.get(), blockPos, blockState),
    IObservingBlockEntity {

    companion object {
        const val REMOTE_PERIPHERALS_TAG = "remotePeripherals"
        const val TARGET_BLOCK_TAG = "targetBlock"
        const val DIRECTION_TAG = "direction"

        fun fromTag(tag: CompoundTag): RemotePeripheralRecord {
            val targetBlock = NbtUtils.readBlockPos(tag.getCompound(TARGET_BLOCK_TAG))
            val direction = Direction.CODEC.byName(tag.getString(DIRECTION_TAG), Direction.NORTH)
            return RemotePeripheralRecord(targetBlock, null, direction)
        }
    }

    data class RemotePeripheralRecord(
        val targetBlock: BlockPos, var peripheralName: String?, val direction: Direction,
        var connectedToListener: Boolean = false, var connectedToPeripheral: Boolean = false, var stack: ItemStack = ItemStack.EMPTY
    ) {
        fun toTag(): CompoundTag {
            val tag = CompoundTag()
            tag.put(TARGET_BLOCK_TAG, NbtUtils.writeBlockPos(targetBlock))
            tag.putString(DIRECTION_TAG, direction.serializedName)

            return tag
        }
    }

    val remotePeripherals: MutableMap<BlockPos, RemotePeripheralRecord> = mutableMapOf()
    private var lastConsumedEventID: Long = BlockStateUpdateEventBus.lastEventID - 1
    private var peripheralConnectionIncomplete: Boolean = false
    private var listenerConnectionIncomplete: Boolean = false

    override fun createPeripheral(side: Direction): PeripheralProxyPeripheral {
        return PeripheralProxyPeripheral(this)
    }

    fun isPosApplicable(pos: BlockPos): Boolean {
        if (pos == this.blockPos)
            return false
        return pos.closerThan(this.blockPos, PeripheralWorksConfig.peripheralProxyMaxRange.toDouble())
    }

    fun containsPos(pos: BlockPos): Boolean {
        return remotePeripherals.contains(pos)
    }

    fun buildPeripheralName(targetPeripheral: IPeripheral): String {
        val naiveName = targetPeripheral.type
        val maxNumber = remotePeripherals.mapNotNull {
            if (it.value.peripheralName?.startsWith(naiveName) == true)
                return@mapNotNull Integer.parseInt(it.value.peripheralName!!.split("_")[1])
            return@mapNotNull null
        }.maxOrNull() ?: -1
        return "${naiveName}_${maxNumber + 1}"
    }

    /**
     * Returns true if block was added to tracking and false if removed
     */
    fun togglePos(pos: BlockPos, direction: Direction, targetPeripheral: IPeripheral): Boolean {
        val blockAdded = if (remotePeripherals.contains(pos)) {
            removePosToTrack(pos)
            false
        } else {
            addPosToTrack(pos, direction, targetPeripheral)
            true
        }
        pushInternalDataChangeToClient()
        return blockAdded
    }

    private fun trackRecord(record: RemotePeripheralRecord, targetPeripheral: IPeripheral) {
        if (!record.connectedToListener) {
            BlockStateUpdateEventBus.addBlockPos(record.targetBlock)
            record.connectedToListener = true
        }
        if (peripheral != null) {
            if (record.peripheralName == null)
                record.peripheralName = buildPeripheralName(targetPeripheral)
            peripheral!!.attachRemotePeripheral(targetPeripheral, record.peripheralName!!)
            record.connectedToPeripheral = true
        } else if (!record.connectedToPeripheral) {
            peripheralConnectionIncomplete = true
        }
        if (level != null && level!!.isClientSide && record.stack.isEmpty) {
            record.stack = level!!.getBlockState(record.targetBlock).block.asItem().defaultInstance
        }
    }

    private fun untrackRecord(record: RemotePeripheralRecord) {
        BlockStateUpdateEventBus.removeBlockPos(record.targetBlock)
        if (record.peripheralName != null)
            peripheral?.removeRemotePeripheral(record.peripheralName!!)
    }

    fun addPosToTrack(pos: BlockPos, direction: Direction, targetPeripheral: IPeripheral) {
        val record = RemotePeripheralRecord(
            pos, buildPeripheralName(targetPeripheral), direction
        )
        remotePeripherals[pos] = record
        trackRecord(record, targetPeripheral)
    }

    fun removePosToTrack(pos: BlockPos): Boolean {
        if (remotePeripherals.contains(pos)) {
            val remoteRecord = remotePeripherals[pos]!!
            remotePeripherals.remove(pos)
            untrackRecord(remoteRecord)
            return true
        }
        return false
    }

    private fun unload() {
        peripheral?.purgePeripheral()
        BlockStateUpdateEventBus.removeBlockPos(remotePeripherals.keys)
    }

    override fun saveInternalData(data: CompoundTag): CompoundTag {
        val trackedBlockTag = ListTag()
        remotePeripherals.forEach {
            trackedBlockTag.add(it.value.toTag())
        }
        data.put(REMOTE_PERIPHERALS_TAG, trackedBlockTag)
        return data
    }

    fun connectBlockPos(level: Level, record: RemotePeripheralRecord) {
        val targetPeripheral = PeripheraliumPlatform.getPeripheral(level, record.targetBlock, Direction.NORTH)
        if (targetPeripheral == null) {
            PeripheralWorksCore.LOGGER.warn("Postpone ${record.targetBlock} for peripheral proxing, it doesn't contains any peripheral for now")
            peripheralConnectionIncomplete = true
        } else {
            trackRecord(record, targetPeripheral)
            pushInternalDataChangeToClient()
        }
    }

    override fun loadInternalData(data: CompoundTag, state: BlockState?): BlockState {
        if (data.contains(REMOTE_PERIPHERALS_TAG)) {
            remotePeripherals.clear()
            val internalList = data.getList(REMOTE_PERIPHERALS_TAG, Tag.TAG_COMPOUND.toInt())
            internalList.forEach {
                val record = fromTag(it as CompoundTag)
                remotePeripherals[record.targetBlock] = record
                if (level == null) {
                    peripheralConnectionIncomplete = true
                    listenerConnectionIncomplete = true
                    PeripheralWorksCore.LOGGER.warn("Postpone of loading blockPos $blockPos")
                } else {
                    connectBlockPos(level!!, record)
                }
            }
        }
        return state ?: blockState
    }

    override fun handleTick(level: Level, pos: BlockPos, state: BlockState) {
        if (listenerConnectionIncomplete || peripheralConnectionIncomplete) {
            remotePeripherals.values.forEach {
                if (!it.connectedToListener || !it.connectedToPeripheral) {
                    connectBlockPos(level, it)
                }
            }
            listenerConnectionIncomplete = remotePeripherals.values.any { !it.connectedToListener }
            peripheralConnectionIncomplete = remotePeripherals.values.any { !it.connectedToPeripheral }
        }
        var dataModified = false
        lastConsumedEventID = BlockStateUpdateEventBus.traverseEvents(lastConsumedEventID) {
            PeripheralWorksCore.LOGGER.warn("Processing event change $it ${it.pos}")
            if (remotePeripherals.contains(it.pos)) {
                val record = remotePeripherals[it.pos]
                PeripheralWorksCore.LOGGER.warn("Extracted record $record for ${it.pos}")
                if (record != null) {
                    untrackRecord(record)
                    val targetPeripheral = PeripheraliumPlatform.getPeripheral(level, it.pos, record.direction)
                    if (targetPeripheral == null) {
                        PeripheralWorksCore.LOGGER.warn("Cannot find peripheral for ${it.pos} purging it completely")
                        dataModified = true
                        remotePeripherals.remove(it.pos)
                    } else {
                        PeripheralWorksCore.LOGGER.warn("Find peripheral $targetPeripheral for ${it.pos}")
                        trackRecord(record, targetPeripheral)
                    }
                }
            }
        }
        if (dataModified) {
            PeripheralWorksCore.LOGGER.warn("Pushing information after removing some tracked position")
            PeripheralWorksCore.LOGGER.warn("$remotePeripherals")
            pushInternalDataChangeToClient()
        }
    }

    override fun setRemoved() {
        unload()
        super.setRemoved()
    }

    override fun destroy() {
        unload()
    }
}