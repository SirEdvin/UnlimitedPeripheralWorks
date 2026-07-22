package site.siredvin.peripheralworks.common.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.state.BlockState
import site.siredvin.peripheralworks.common.setup.BlockEntityTypes
import site.siredvin.peripheralworks.computercraft.peripherals.HologramProjectorPeripheral
import site.siredvin.tweakium.modules.peripheral.blockentity.StatefulPeripheralBlockEntity
import site.siredvin.tweakium.modules.peripheral.owner.BlockEntityPeripheralOwner

class HologramProjectorBlockEntity(blockPos: BlockPos, blockState: BlockState) : StatefulPeripheralBlockEntity<HologramProjectorPeripheral>(BlockEntityTypes.HOLOGRAM_PROJECTOR.get(), blockPos, blockState) {
    override fun createPeripheral(side: Direction): HologramProjectorPeripheral = HologramProjectorPeripheral(
        BlockEntityPeripheralOwner(this),
    )
}
