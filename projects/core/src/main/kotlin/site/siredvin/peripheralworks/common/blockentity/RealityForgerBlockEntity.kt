package site.siredvin.peripheralworks.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.computercraft.peripherals.RealityForgerPeripheral
import site.siredvin.tweakium.modules.peripheral.blockentity.PeripheralBlockEntity

class RealityForgerBlockEntity(blockPos: BlockPos, blockState: BlockState) : PeripheralBlockEntity<RealityForgerPeripheral>(BlockEntityTypes.REALITY_FORGER.get(), blockPos, blockState) {
    override fun createPeripheral(side: Direction): RealityForgerPeripheral = RealityForgerPeripheral(this)
}
