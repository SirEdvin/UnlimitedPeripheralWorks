package site.siredvin.peripheralworks.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralium.common.blockentities.PeripheralBlockEntity
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.computercraft.peripherals.UltimateSensorPeripheral

class UltimateSensorBlockEntity(blockPos: BlockPos, blockState: BlockState) :
    PeripheralBlockEntity<UltimateSensorPeripheral>(BlockEntityTypes.ULTIMATE_SENSOR.get(), blockPos, blockState) {
    override fun createPeripheral(side: Direction): UltimateSensorPeripheral {
        return UltimateSensorPeripheral.of(this)
    }
}