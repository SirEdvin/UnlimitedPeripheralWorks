package site.siredvin.peripheralworks.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.computercraft.peripherals.UltimateSensorPeripheral
import site.siredvin.tweakium.modules.peripheral.blockentity.PeripheralBlockEntity

class UltimateSensorBlockEntity(blockPos: BlockPos, blockState: BlockState) : PeripheralBlockEntity<UltimateSensorPeripheral>(BlockEntityTypes.ULTIMATE_SENSOR.get(), blockPos, blockState) {
    override fun createPeripheral(side: Direction): UltimateSensorPeripheral = UltimateSensorPeripheral.of(this)
}
