package site.siredvin.peripheralworks.integrations.gtceu

import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider
import com.gregtechceu.gtceu.integration.cctweaked.peripherals.EnergyInfoPeripheral
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

class EnergyInfoPeripheralPlugin(private val capability: IEnergyInfoProvider) : IPeripheralPlugin {
    companion object {
        const val TYPE = "gtceu:energy_info"
    }

    override val additionalType: String
        get() = TYPE

    @LuaFunction(mainThread = true)
    fun getEnergyStored(): MethodResult = EnergyInfoPeripheral.getEnergyStored(capability)

    @LuaFunction(mainThread = true)
    fun getEnergyCapacity(): MethodResult = EnergyInfoPeripheral.getEnergyCapacity(capability)

    @LuaFunction(mainThread = true)
    fun getInputPerSec(): MethodResult = EnergyInfoPeripheral.getInputPerSec(capability)

    @LuaFunction(mainThread = true)
    fun getOutputPerSec(): MethodResult = EnergyInfoPeripheral.getOutputPerSec(capability)
}
