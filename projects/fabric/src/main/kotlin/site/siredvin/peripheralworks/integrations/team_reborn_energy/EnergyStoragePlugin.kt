package site.siredvin.peripheralworks.integrations.team_reborn_energy

import dan200.computercraft.api.lua.LuaFunction
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import team.reborn.energy.api.EnergyStorage

class EnergyStoragePlugin(private val energyStorage: EnergyStorage) : IPeripheralPlugin {

    companion object {
        const val PLUGIN_TYPE = "energy_storage"
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

    @LuaFunction
    fun getEnergyUnits(): String {
        return ""
    }
}
