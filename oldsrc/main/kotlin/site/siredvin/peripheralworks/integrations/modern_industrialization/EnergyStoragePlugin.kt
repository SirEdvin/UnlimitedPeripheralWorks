package site.siredvin.peripheralworks.integrations.modern_industrialization

import aztech.modern_industrialization.compat.megane.holder.EnergyComponentHolder
import aztech.modern_industrialization.machines.blockentities.EnergyFromFluidMachineBlockEntity
import aztech.modern_industrialization.machines.blockentities.multiblocks.ElectricBlastFurnaceBlockEntity
import aztech.modern_industrialization.machines.components.EnergyComponent
import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralworks.api.PeripheralPluginProvider
import team.reborn.energy.api.EnergyStorage

class EnergyStoragePlugin(private val energyStorage: EnergyComponent): IPeripheralPlugin {

    companion object {
        const val PLUGIN_TYPE = "energy_storage"
    }

    class Provider: PeripheralPluginProvider {
        override val pluginType: String
            get() = PLUGIN_TYPE

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!Configuration.enableEnergyStorage)
                return null
            val blockEntity = level.getBlockEntity(pos) ?: return null
            if (blockEntity is EnergyComponentHolder)
                return EnergyStoragePlugin(blockEntity.energyComponent)
            return null
        }

    }


    override val additionalType: String
        get() = PLUGIN_TYPE

    @LuaFunction(mainThread = true)
    fun getEnergy(): Long {
        return energyStorage.eu
    }

    @LuaFunction(mainThread = true)
    fun getEnergyCapacity(): Long {
        return energyStorage.capacity
    }

    @LuaFunction
    fun getEnergyUnits(): String {
        return "EU"
    }
}