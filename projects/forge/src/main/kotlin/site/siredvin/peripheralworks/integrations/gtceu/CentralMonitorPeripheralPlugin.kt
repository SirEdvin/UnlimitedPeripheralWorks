package site.siredvin.peripheralworks.integrations.gtceu

import com.gregtechceu.gtceu.api.capability.ICentralMonitor
import com.gregtechceu.gtceu.integration.cctweaked.peripherals.CentralMonitorPeripheral
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

class CentralMonitorPeripheralPlugin(private val capability: ICentralMonitor) : IPeripheralPlugin {
    companion object {
        const val TYPE = "gtceu:central_monitor"
    }

    override val additionalType: String
        get() = TYPE

    @LuaFunction(mainThread = true)
    fun getGroups(): MethodResult = CentralMonitorPeripheral.getGroups(capability)
}
