package site.siredvin.peripheralworks.computercraft

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.extra.plugins.FluidStoragePlugin
import site.siredvin.peripheralium.extra.plugins.PeripheralPluginUtils
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig

object FluidStorageProvider: PeripheralPluginProvider {
    override val pluginType: String
        get() = PeripheralPluginUtils.Type.FLUID_STORAGE

    override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
        if (!PeripheralWorksConfig.enableGenericFluidStorage)
            return null
        val fluidStorage = FluidStorage.SIDED.find(level, pos, side) ?: return null
        return FluidStoragePlugin(level, fluidStorage, PeripheralWorksConfig.fluidStorageTransferLimit)
    }

}