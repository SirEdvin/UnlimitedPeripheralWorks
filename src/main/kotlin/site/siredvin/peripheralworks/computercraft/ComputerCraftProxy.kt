package site.siredvin.peripheralworks.computercraft

import dan200.computercraft.api.peripheral.IPeripheral
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level

object ComputerCraftProxy {
    fun peripheralProvider(level: Level, pos: BlockPos, side: Direction): IPeripheral? {
        val entity = level.getBlockEntity(pos)
        val fluidStorage = FluidStorage.SIDED.find(level, pos, side)
        if (fluidStorage != null) {
            val peripheral = NewIntegrationPeripheral(entity)
            peripheral.reallyAddPlugin(GenericFluidTransferPlugin(fluidStorage))
            return peripheral
        }
        return null
    }
}