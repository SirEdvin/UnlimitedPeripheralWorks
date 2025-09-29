package site.siredvin.peripheralworks.common.blockentity

import dan200.computercraft.api.network.wired.WiredElement
import dan200.computercraft.api.peripheral.IPeripheral
import dan200.computercraft.shared.computer.core.ServerContext
import dan200.computercraft.shared.peripheral.modem.wired.WiredModemElement
import dan200.computercraft.shared.platform.ComponentAccess
import dan200.computercraft.shared.platform.PlatformHelper
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import site.siredvin.broccolium.modules.base.api.IObservingBlockEntity
import site.siredvin.broccolium.modules.base.ext.toVec3
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.peripheralworks.PeripheralWorksCore
import site.siredvin.peripheralworks.common.block.PeripheralProxy
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.common.events.BlockStateUpdateEventBus
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.computercraft.peripherals.PeripheralProxyPeripheral
import site.siredvin.tweakium.modules.peripheral.blockentity.MutablePeripheralBlockEntity
import site.siredvin.tweakium.modules.platform.ComputerPlatformToolkit
import java.util.function.Consumer

class PeripheralProxyBlockEntity(blockPos: BlockPos, blockState: BlockState) :
    MutablePeripheralBlockEntity<PeripheralProxyPeripheral>(BlockEntityTypes.PERIPHERAL_PROXY.get(), blockPos, blockState),
    IObservingBlockEntity {

    companion object {
        const val REMOTE_PERIPHERALS_TAG = "remotePeripherals"
        const val TARGET_BLOCK_TAG = "targetBlock"
        const val DIRECTION_TAG = "direction"
        const val PERIPHERAL_NAME_TAG = "peripheralName"
        const val RESERVED_IDS_TAG = "reservedIds"

        fun fromTag(tag: CompoundTag): RemotePeripheralRecord {
            val targetBlock = NbtUtils.readBlockPos(tag.getCompound(TARGET_BLOCK_TAG))
            val direction = Direction.CODEC.byName(tag.getString(DIRECTION_TAG), Direction.NORTH)
            val peripheralName: String? = if (tag.contains(PERIPHERAL_NAME_TAG)) {
                tag.getString(PERIPHERAL_NAME_TAG)
            } else {
                null
            }
            return RemotePeripheralRecord(targetBlock, peripheralName, direction)
        }
    }

    class PeripheralProxyWiredElement(private val be: PeripheralProxyBlockEntity) : WiredModemElement() {
        override fun attachPeripheral(
            name: String,
            peripheral: IPeripheral,
        ) {
            be.peripheral?.attachRemotePeripheral(peripheral, name)
        }

        override fun detachPeripheral(name: String) {
            be.peripheral?.removeRemotePeripheral(name)
        }

        override fun getLevel(): Level? = be.level

        override fun getPosition(): Vec3 = be.blockPos.toVec3()
    }

    data class RemotePeripheralRecord(
        val targetBlock: BlockPos,
        var peripheralName: String?,
        val direction: Direction,
        var connectedToListener: Boolean = false,
        var connectedToPeripheral: Boolean = false,
        var stack: ItemStack = ItemStack.EMPTY,
    ) {
        fun toTag(): CompoundTag {
            val tag = CompoundTag()
            tag.put(TARGET_BLOCK_TAG, NbtUtils.writeBlockPos(targetBlock))
            tag.putString(DIRECTION_TAG, direction.serializedName)
            if (peripheralName != null) tag.putString(PERIPHERAL_NAME_TAG, peripheralName!!)
            return tag
        }
    }

    val remotePeripherals: MutableMap<BlockPos, RemotePeripheralRecord> = mutableMapOf()
    val element = PeripheralProxyWiredElement(this)
    private val connectedElements: ComponentAccess<WiredElement?> =
        PlatformHelper.get().createWiredElementAccess(
            this,
            Consumer { x: Direction ->
                if (x == this.blockState.getValue(
                        PeripheralProxy.ORIENTATION,
                    ).opposite
                ) {
                    scheduleConnectionsChanged()
                }
            },
        )
    private var lastConsumedEventID: Long = BlockStateUpdateEventBus.lastEventID - 1
    private var peripheralConnectionIncomplete: Boolean = false
    private var listenerConnectionIncomplete: Boolean = false
    private var refreshConnectionsRequired: Boolean = true
    var itemStackCacheBuilt: Boolean = false

    override fun createPeripheral(side: Direction): PeripheralProxyPeripheral = PeripheralProxyPeripheral(this)

    fun isPosApplicable(pos: BlockPos): Boolean {
        if (pos == this.blockPos) {
            return false
        }
        return pos.closerThan(this.blockPos, PeripheralWorksConfig.peripheralProxyMaxRange.toDouble())
    }

    fun containsPos(pos: BlockPos): Boolean = remotePeripherals.contains(pos)

    private fun buildPeripheralName(targetPeripheral: IPeripheral): String {
        val newPeripheralID = ServerContext.get(PlatformToolkit.get().minecraftServer!!).getNextId("peripheral.${targetPeripheral.type}")
        return "${targetPeripheral.type}_$newPeripheralID"
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

    private fun updatePeripherals() {
        if (peripheral != null) {
            PeripheralWorksCore.logger.warn("Update peripheral called, ${peripheral!!.peripheralsRecord.keys}")
            element.node.updatePeripherals(peripheral!!.peripheralsRecord.mapValues { it.value.peripheral })
        }
    }

    private fun trackRecord(record: RemotePeripheralRecord, targetPeripheral: IPeripheral) {
        if (!record.connectedToListener) {
            BlockStateUpdateEventBus.addBlockPos(record.targetBlock)
            record.connectedToListener = true
        }
        if (peripheral != null) {
            if (record.peripheralName == null) {
                record.peripheralName = buildPeripheralName(targetPeripheral)
            }
            peripheral!!.attachRemotePeripheral(targetPeripheral, record.peripheralName!!, useInternalID = true)
            this.updatePeripherals()
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
        if (record.peripheralName != null) {
            peripheral?.removeRemotePeripheral(record.peripheralName!!)
            this.updatePeripherals()
        }
    }

    fun addPosToTrack(pos: BlockPos, direction: Direction, targetPeripheral: IPeripheral) {
        val record = RemotePeripheralRecord(
            pos,
            buildPeripheralName(targetPeripheral),
            direction,
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

    private fun scheduleConnectionsChanged() {
        refreshConnectionsRequired = true
    }

    override fun onNeighbourChange(neighbour: BlockPos) {
        if (blockPos.relative(blockState.getValue(PeripheralProxy.ORIENTATION).opposite) == neighbour) {
            refreshConnectionsRequired = true
        }
    }

    fun connectBlockPos(level: Level, record: RemotePeripheralRecord) {
        if (level is ServerLevel) {
            val targetPeripheral = ComputerPlatformToolkit.get().getPeripheral(level, record.targetBlock, Direction.NORTH)
            if (targetPeripheral == null) {
                PeripheralWorksCore.logger.debug(
                    "Postpone {} for peripheral proxing, it doesn't contains any peripheral for now",
                    record.targetBlock,
                )
                peripheralConnectionIncomplete = true
            } else {
                trackRecord(record, targetPeripheral)
                pushInternalDataChangeToClient()
            }
        }
    }

    fun updateCachedStacks(level: Level) {
        if (level.isClientSide) {
            itemStackCacheBuilt = true
            remotePeripherals.values.forEach {
                if (it.stack.isEmpty) {
                    it.stack = level.getBlockState(it.targetBlock).block.asItem().defaultInstance
                    if (it.stack.isEmpty) {
                        itemStackCacheBuilt = false
                    }
                }
            }
        }
    }

    override fun saveInternalData(data: CompoundTag): CompoundTag {
        val trackedBlockTag = ListTag()
        remotePeripherals.forEach {
            trackedBlockTag.add(it.value.toTag())
        }
        data.put(REMOTE_PERIPHERALS_TAG, trackedBlockTag)
        return data
    }

    private fun refreshConnection() {
        val level = getLevel() ?: return
        if (level.isClientSide) return
        refreshConnectionsRequired = false

        val current = blockPos

        val direction = blockState.getValue(PeripheralProxy.ORIENTATION).opposite
        val offset = current.relative(direction)
        if (!level.isLoaded(offset)) return

        val element = connectedElements.get(direction) ?: return
        PeripheralWorksCore.logger.warn("connection is called")
        this.element.node.connectTo(element.node)
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
                    PeripheralWorksCore.logger.debug("Postpone of loading blockPos {}", blockPos)
                } else {
                    connectBlockPos(level!!, record)
                }
            }
            if (level?.isClientSide == true) {
                updateCachedStacks(level!!)
            }
        }
        return state ?: blockState
    }

    override fun handleTick(level: Level, pos: BlockPos, state: BlockState) {
        if (peripheral == null) {
            ensurePeripheralCreated(Direction.UP)
        }
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
            PeripheralWorksCore.logger.debug("Processing event change {} {}", it, it.pos)
            if (remotePeripherals.contains(it.pos)) {
                val record = remotePeripherals[it.pos]
                PeripheralWorksCore.logger.debug("Extracted record {} for {}", record, it.pos)
                if (record != null) {
                    untrackRecord(record)
                    if (level is ServerLevel) {
                        val targetPeripheral = ComputerPlatformToolkit.get().getPeripheral(level, it.pos, record.direction)
                        if (targetPeripheral == null) {
                            PeripheralWorksCore.logger.debug(
                                "Cannot find peripheral for {} purging it completely",
                                it.pos,
                            )
                            dataModified = true
                            remotePeripherals.remove(it.pos)
                        } else {
                            PeripheralWorksCore.logger.debug("Find peripheral {} for {}", targetPeripheral, it.pos)
                            trackRecord(record, targetPeripheral)
                        }
                    }
                }
            }
        }
        if (dataModified) {
            PeripheralWorksCore.logger.warn("Pushing information after removing some tracked position")
            PeripheralWorksCore.logger.warn("$remotePeripherals")
            pushInternalDataChangeToClient()
        }
        if (refreshConnectionsRequired) {
            refreshConnection()
        }
    }

    override fun setRemoved() {
        unload()
        if (level == null || !level!!.isClientSide) {
            this.element.node.remove()
        }
        super.setRemoved()
    }

    override fun destroy() {
        if (level == null || !level!!.isClientSide) {
            this.element.node.remove()
        }
        unload()
    }
}
