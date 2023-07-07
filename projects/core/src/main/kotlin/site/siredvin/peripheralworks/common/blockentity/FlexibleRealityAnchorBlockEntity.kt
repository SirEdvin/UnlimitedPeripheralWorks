package site.siredvin.peripheralworks.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BooleanProperty
import site.siredvin.peripheralium.common.blockentities.MutableNBTBlockEntity
import site.siredvin.peripheralium.computercraft.peripheral.OwnedPeripheral
import site.siredvin.peripheralium.xplat.XplatRegistries
import site.siredvin.peripheralworks.common.block.FlexibleRealityAnchor
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.tags.BlockTags
import kotlin.math.max
import kotlin.math.min

open class FlexibleRealityAnchorBlockEntity(blockPos: BlockPos, blockState: BlockState) :
    MutableNBTBlockEntity<OwnedPeripheral<*>>(BlockEntityTypes.FLEXIBLE_REALITY_ANCHOR.get(), blockPos, blockState) {

    companion object {
        const val MIMIC_TAG = "mimic"
        const val LIGHT_LEVEL_TAG = "lightLevel"
    }

    private var _mimic: BlockState? = null
    private var _lightLevel = 0
    private var pendingState: BlockState? = null

    val mimic: BlockState?
        get() = this._mimic

    var lightLevel: Int
        get() = this._lightLevel
        set(value) {
            this._lightLevel = max(0, min(value, 15))
        }

    fun setMimic(mimic: BlockState?, state: BlockState? = null, skipUpdate: Boolean = false): Boolean {
        val realState = state ?: pendingState ?: blockState
        if (mimic != null) {
            if (mimic.`is`(BlockTags.REALITY_FORGER_FORBIDDEN)) {
                return false
            }
        }
        this._mimic = mimic
        if (!skipUpdate) {
            pushInternalDataChangeToClient(realState.setValue(FlexibleRealityAnchor.CONFIGURED, mimic != null))
        } else {
            if (pendingState == null) pendingState = realState
            pendingState = pendingState!!.setValue(FlexibleRealityAnchor.CONFIGURED, mimic != null)
        }
        return true
    }

    override fun getPeripheral(side: Direction): OwnedPeripheral<*>? {
        return null
    }

    override fun createPeripheral(side: Direction): OwnedPeripheral<*> {
        throw IllegalCallerException("You should not call this function at all")
    }

    override fun loadInternalData(data: CompoundTag, state: BlockState?): BlockState {
        if (data.contains(MIMIC_TAG)) {
            val setResult = setMimic(NbtUtils.readBlockState(XplatRegistries.BLOCKS, data.getCompound(MIMIC_TAG)), state ?: blockState, true)
            if (setResult && level?.isClientSide == true) triggerRenderUpdate()
        }
        if (data.contains(LIGHT_LEVEL_TAG)) {
            this.lightLevel = data.getInt(LIGHT_LEVEL_TAG)
        }
        return pendingState!!
    }

    override fun saveInternalData(data: CompoundTag): CompoundTag {
        if (_mimic != null) {
            data.put(MIMIC_TAG, NbtUtils.writeBlockState(_mimic!!))
        }
        if (lightLevel != 0) data.putInt(LIGHT_LEVEL_TAG, lightLevel)
        return data
    }

    fun setBooleanStateValue(stateValue: BooleanProperty, value: Boolean) {
        if (pendingState == null) pendingState = blockState
        pendingState = pendingState!!.setValue(stateValue, value)
    }
}
