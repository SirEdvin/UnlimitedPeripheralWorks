package site.siredvin.peripheralworks.computercraft

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraftforge.common.capabilities.ForgeCapabilities
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.extra.plugins.ForgeEnergyPlugin
import site.siredvin.peripheralium.extra.plugins.ForgeFluidStoragePlugin
import site.siredvin.peripheralium.extra.plugins.PeripheralPluginUtils
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig

object FluidStorageProvider : PeripheralPluginProvider {
    override val pluginType: String
        get() = PeripheralPluginUtils.Type.FLUID_STORAGE

    override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
        if (!PeripheralWorksConfig.enableGenericFluidStorage) {
            return null
        }
        val blockEntity = level.getBlockEntity(pos) ?: return null
        val cap = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER)
        if (!cap.isPresent) {
            return null
        }
        return ForgeFluidStoragePlugin(cap.orElseThrow { NullPointerException() }, PeripheralWorksConfig.fluidStorageTransferLimit)
    }
}

object EnergyStorageProvider : PeripheralPluginProvider {
    override val pluginType: String
        get() = PeripheralPluginUtils.Type.ENERGY_STORAGE

    override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
        if (!PeripheralWorksConfig.enableGenericFluidStorage) {
            return null
        }
        val blockEntity = level.getBlockEntity(pos) ?: return null
        val cap = blockEntity.getCapability(ForgeCapabilities.ENERGY)
        if (!cap.isPresent) {
            return null
        }
        return ForgeEnergyPlugin(cap.orElseThrow { NullPointerException() })
    }
}
