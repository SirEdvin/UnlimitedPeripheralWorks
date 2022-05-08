package site.siredvin.peripheralworks.integrations.team_reborn_energy

import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import team.reborn.energy.api.EnergyStorage

class EnergyStoragePlugin(private val energyStorage: EnergyStorage): IPeripheralPlugin {

    companion object {
        const val PLUGIN_TYPE = "energy_storage"
    }

    class Provider: PeripheralPluginProvider {
        override val pluginType: String
            get() = PLUGIN_TYPE

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!Configuration.enableEnergyStorage)
                return null
            val energyStorage = EnergyStorage.SIDED.find(level, pos, side) ?: return null
            return EnergyStoragePlugin(energyStorage)
        }

    }


    override val additionalType: String
        get() = PLUGIN_TYPE

    @LuaFunction(mainThread = true)
    fun getEnergy(): Long {
        return energyStorage.amount
    }

    @LuaFunction(mainThread = true)
    fun getEnergyCapacity(): Long {
        return energyStorage.capacity
    }
}