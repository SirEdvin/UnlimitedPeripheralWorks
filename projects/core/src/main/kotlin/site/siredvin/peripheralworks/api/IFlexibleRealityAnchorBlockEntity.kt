package site.siredvin.peripheralworks.api

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BooleanProperty
import site.siredvin.peripheralium.api.blockentities.ISyncingBlockEntity

interface IFlexibleRealityAnchorBlockEntity : ISyncingBlockEntity {
    var lightLevel: Int
    val mimic: BlockState?
    fun setMimic(mimic: BlockState?, state: BlockState? = null, skipUpdate: Boolean = false)
    fun setBooleanStateValue(stateValue: BooleanProperty, value: Boolean)
}
