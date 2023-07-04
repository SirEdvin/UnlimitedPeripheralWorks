package site.siredvin.peripheralworks.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralium.common.blockentities.PeripheralBlockEntity
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.computercraft.peripherals.RealityForgerPeripheral

class RealityForgerBlockEntity(blockPos: BlockPos, blockState: BlockState) :
    PeripheralBlockEntity<RealityForgerPeripheral>(BlockEntityTypes.REALITY_FORGER.get(), blockPos, blockState) {
    override fun createPeripheral(side: Direction): RealityForgerPeripheral {
        return RealityForgerPeripheral(this)
    }
}
