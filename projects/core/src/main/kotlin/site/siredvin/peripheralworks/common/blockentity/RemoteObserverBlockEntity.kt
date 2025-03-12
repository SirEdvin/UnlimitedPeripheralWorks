package site.siredvin.peripheralworks.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.*
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.broccolium.modules.base.api.IObservingBlockEntity
import site.siredvin.broccolium.modules.base.block.FacingBlockEntityBlock
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.common.events.BlockStateUpdateEventBus
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.computercraft.peripherals.RemoteObserverPeripheral
import site.siredvin.tweakium.modules.peripheral.blockentity.MutablePeripheralBlockEntity
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation
import site.siredvin.tweakium.modules.peripheral.representation.stateProperties
import java.util.*

class RemoteObserverBlockEntity(blockPos: BlockPos, blockState: BlockState) :
    MutablePeripheralBlockEntity<RemoteObserverPeripheral>(BlockEntityTypes.REMOTE_OBSERVER.get(), blockPos, blockState),
    IObservingBlockEntity {

    companion object {
        const val TRACKED_BLOCKS_TAG = "trackedBlocks"
    }

    private var lastConsumedEventID: Long = BlockStateUpdateEventBus.lastEventID - 1
    private val trackedBlocks: MutableList<BlockPos> = mutableListOf()

    private val facing: Direction
        get() = blockState.getValue(FacingBlockEntityBlock.FACING)

    val trackedBlocksView: List<BlockPos>
        get() = trackedBlocks

    override fun createPeripheral(side: Direction): RemoteObserverPeripheral = RemoteObserverPeripheral(this)

    fun isPosApplicable(pos: BlockPos): Boolean {
        if (pos == this.blockPos) {
            return false
        }
        return pos.closerThan(this.blockPos, PeripheralWorksConfig.remoteObserverMaxRange.toDouble())
    }

    /**
     * Returns true if block was added to tracking and false if removed
     */
    fun togglePos(pos: BlockPos): Boolean {
        val blockAdded = if (trackedBlocks.contains(pos)) {
            removePosToTrack(pos)
            false
        } else {
            addPosToTrack(pos)
            true
        }
        pushInternalDataChangeToClient()
        return blockAdded
    }

    fun addPosToTrack(pos: BlockPos) {
        trackedBlocks.add(pos)
        BlockStateUpdateEventBus.addBlockPos(pos)
    }

    fun removePosToTrack(pos: BlockPos) {
        if (trackedBlocks.remove(pos)) {
            BlockStateUpdateEventBus.removeBlockPos(pos)
        }
    }

    private fun unload() {
        BlockStateUpdateEventBus.removeBlockPos(trackedBlocks)
    }

    override fun saveInternalData(data: CompoundTag): CompoundTag {
        val trackedBlockTag = ListTag()
        trackedBlocks.forEach { trackedBlockTag.add(NbtUtils.writeBlockPos(it)) }
        data.put(TRACKED_BLOCKS_TAG, trackedBlockTag)
        return data
    }

    override fun loadInternalData(data: CompoundTag, state: BlockState?): BlockState {
        if (data.contains(TRACKED_BLOCKS_TAG)) {
            val internalList = data.getList(TRACKED_BLOCKS_TAG, Tag.TAG_INT_ARRAY.toInt())
            internalList.forEach {
                val list = (it as IntArrayTag).asIntArray
                addPosToTrack(BlockPos(list[0], list[1], list[2]))
            }
        }
        return state ?: blockState
    }

    override fun handleTick(level: Level, pos: BlockPos, state: BlockState) {
        lastConsumedEventID = BlockStateUpdateEventBus.traverseEvents(lastConsumedEventID) {
            connectedComputers.forEach { computer ->
                val previousState = LuaRepresentation.forBlockState(it.previous)
                val currentState = LuaRepresentation.forBlockState(it.current)
                stateProperties.accept(it.previous, previousState)
                stateProperties.accept(it.current, currentState)
                computer.queueEvent(
                    "block_change",
                    LuaRepresentation.forBlockPos(it.pos, facing, blockPos),
                    previousState,
                    currentState,
                )
            }
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
