package site.siredvin.peripheralworks.computercraft.peripherals

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import site.siredvin.broccolium.modules.base.block.FacingBlockEntityBlock
import site.siredvin.peripheralworks.common.blockentity.RemoteObserverBlockEntity
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.tweakium.modules.peripheral.OwnedPeripheral
import site.siredvin.tweakium.modules.peripheral.owner.BlockEntityPeripheralOwner
import site.siredvin.tweakium.modules.peripheral.representation.LuaInterpretation
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation

class RemoteObserverPeripheral(
    private val blockEntity: RemoteObserverBlockEntity,
) : OwnedPeripheral<BlockEntityPeripheralOwner<RemoteObserverBlockEntity>>(TYPE, BlockEntityPeripheralOwner(blockEntity)) {
    companion object {
        const val TYPE = "remote_observer"
    }
    override val isEnabled: Boolean
        get() = PeripheralWorksConfig.enableRemoteObserver

    override val peripheralConfiguration: MutableMap<String, Any>
        get() {
            val base = super.peripheralConfiguration
            base["maxRange"] = PeripheralWorksConfig.remoteObserverMaxRange
            return base
        }

    @LuaFunction(mainThread = true)
    fun addPosition(pos: Map<*, *>): MethodResult {
        val targetPos = LuaInterpretation.asBlockPos(
            peripheralOwner.pos,
            pos,
            blockEntity.blockState.getValue(
                FacingBlockEntityBlock.FACING,
            ),
        )
        if (!blockEntity.isPosApplicable(targetPos)) {
            return MethodResult.of(false, "Position too far away")
        }
        blockEntity.addPosToTrack(targetPos)
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun removePosition(pos: Map<*, *>): MethodResult {
        blockEntity.removePosToTrack(
            LuaInterpretation.asBlockPos(peripheralOwner.pos, pos, blockEntity.blockState.getValue(FacingBlockEntityBlock.FACING)),
        )
        return MethodResult.of(true)
    }

    @LuaFunction(mainThread = true)
    fun getPositions(): List<Map<String, Any>> = blockEntity.trackedBlocksView.map {
        LuaRepresentation.forBlockPos(
            it,
            blockEntity.blockState.getValue(FacingBlockEntityBlock.FACING),
            peripheralOwner.pos,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RemoteObserverPeripheral) return false
        if (!super.equals(other)) return false

        if (blockEntity != other.blockEntity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + blockEntity.hashCode()
        return result
    }
}
