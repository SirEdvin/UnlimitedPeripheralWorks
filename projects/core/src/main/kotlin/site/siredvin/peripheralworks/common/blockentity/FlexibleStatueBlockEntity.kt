package site.siredvin.peripheralworks.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.VoxelShape
import site.siredvin.peripheralium.common.blockentities.MutableNBTBlockEntity
import site.siredvin.peripheralium.computercraft.peripheral.OwnedPeripheral
import site.siredvin.peripheralworks.common.block.FlexibleStatue
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.utils.QuadList

class FlexibleStatueBlockEntity(blockPos: BlockPos, blockState: BlockState) : MutableNBTBlockEntity<OwnedPeripheral<*>>(BlockEntityTypes.FLEXIBLE_STATUE.get(), blockPos, blockState) {
    companion object {
        val BAKED_QUADS_TAG = "bakedQuads"
        val NAME_TAG = "statueName"
        val AUTHOR_TAG = "statueAuthor"
        val LIGHT_LEVEL_TAG = "lightLevel"
    }

    private var pendingState: BlockState? = null
    private var _bakedQuads: QuadList? = null
    private var _blockShape: VoxelShape? = null

    var name: String? = null
    var author: String? = null
    var lightLevel = 0
    val blockShape: VoxelShape?
        get() = _blockShape

    val bakedQuads: QuadList?
        get() = _bakedQuads

    val facing: Direction
        get() = blockState.getValue(FlexibleStatue.FACING)

    override fun getPeripheral(side: Direction): OwnedPeripheral<*>? {
        return null
    }

    override fun createPeripheral(side: Direction): OwnedPeripheral<*> {
        throw IllegalCallerException("You should not call this function at all")
    }

    override fun loadInternalData(data: CompoundTag, state: BlockState?): BlockState {
        val mutableState = state ?: blockState
        if (data.contains(NAME_TAG)) name = data.getString(NAME_TAG)
        if (data.contains(AUTHOR_TAG)) author = data.getString(AUTHOR_TAG)
        if (data.contains(LIGHT_LEVEL_TAG)) lightLevel = data.getInt(LIGHT_LEVEL_TAG)
        if (data.contains(BAKED_QUADS_TAG)) {
            val newBakedQuads = QuadList(data.getList(BAKED_QUADS_TAG, 10))
            if (_bakedQuads != newBakedQuads) {
                setBakedQuads(newBakedQuads, mutableState, true)
            }
        }
        return mutableState
    }

    override fun saveInternalData(data: CompoundTag): CompoundTag {
        if (_bakedQuads != null) {
            data.put(BAKED_QUADS_TAG, _bakedQuads!!.toTag())
        }
        if (name != null) {
            data.putString(NAME_TAG, name!!)
        }
        if (author != null) {
            data.putString(AUTHOR_TAG, author!!)
        }
        if (lightLevel != 0) data.putInt(LIGHT_LEVEL_TAG, lightLevel)
        return data
    }

    fun setBakedQuads(bakedQuads: QuadList, skipUpdate: Boolean) {
        setBakedQuads(bakedQuads, blockState, skipUpdate)
    }

    fun setBakedQuads(bakedQuads: QuadList, state: BlockState, skipUpdate: Boolean) {
        this._bakedQuads = bakedQuads
        refreshShape()
        if (!skipUpdate) {
            pushInternalDataChangeToClient(state.setValue(FlexibleStatue.CONFIGURED, true))
        } else {
            if (pendingState == null) pendingState = state
            pendingState = pendingState!!.setValue(FlexibleStatue.CONFIGURED, true)
        }
    }

    fun clear(state: BlockState, skipUpdate: Boolean) {
        _bakedQuads = null
        refreshShape()
        if (!skipUpdate) {
            pushInternalDataChangeToClient(state.setValue(FlexibleStatue.CONFIGURED, false))
        } else {
            if (pendingState == null) pendingState = state
            pendingState = pendingState!!.setValue(FlexibleStatue.CONFIGURED, false)
        }
    }

    fun refreshShape() {
        _blockShape = _bakedQuads?.shape
    }
}
