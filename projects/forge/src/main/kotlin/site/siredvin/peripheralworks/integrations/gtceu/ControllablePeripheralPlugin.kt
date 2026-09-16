package site.siredvin.peripheralworks.integrations.gtceu

import com.gregtechceu.gtceu.api.capability.IControllable
import com.gregtechceu.gtceu.integration.cctweaked.peripherals.ControllablePeripheral
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

class ControllablePeripheralPlugin(private val controllable: IControllable) : IPeripheralPlugin {
    companion object {
        const val TYPE = "gtceu:controllable"
    }

    override val additionalType: String
        get() = TYPE

    @LuaFunction(mainThread = true)
    fun isWorkingEnabled(): MethodResult = ControllablePeripheral.isWorkingEnabled(controllable)

    @LuaFunction(mainThread = true)
    fun setWorkingEnabled(enabled: Boolean) {
        ControllablePeripheral.setWorkingEnabled(controllable, enabled)
    }

    @LuaFunction(mainThread = true)
    fun setSuspendAfterFinish(suspendAfterFinish: Boolean) {
        ControllablePeripheral.setSuspendAfterFinish(controllable, suspendAfterFinish)
    }
}
