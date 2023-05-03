package site.siredvin.peripheralworks.integrations.team_reborn_energy

import dan200.computercraft.api.ComputerCraftAPI
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import site.siredvin.peripheralworks.common.configuration.PeripheralWorksConfig
import site.siredvin.peripheralworks.computercraft.ComputerCraftProxy
import team.reborn.energy.api.EnergyStorage

class Integration: Runnable {
    object EnergyPluginProvider: PeripheralPluginProvider {
        override val pluginType: String
            get() = EnergyStoragePlugin.PLUGIN_TYPE

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!Configuration.enableEnergyStorage)
                return null
            val energyStorage = EnergyStorage.SIDED.find(level, pos, side) ?: return null
            return EnergyStoragePlugin(energyStorage)
        }

    }

    override fun run() {
        ComputerCraftProxy.addProvider(EnergyPluginProvider)
        ComputerCraftAPI.registerRefuelHandler(EnergyRefuelHandler)
        PeripheralWorksConfig.registerIntegrationConfiguration("team_reborn_energy", Configuration)
    }
}