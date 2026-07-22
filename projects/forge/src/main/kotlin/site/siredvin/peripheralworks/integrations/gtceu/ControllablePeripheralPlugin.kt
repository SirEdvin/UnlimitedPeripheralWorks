package site.siredvin.peripheralworks.integrations.gtceu

import com.gregtechceu.gtceu.api.capability.IControllable
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

class ControllablePeripheralPlugin(private val controllable: IControllable) : IPeripheralPlugin {
    companion object {
        const val TYPE = "gtceu:controllable"
    }

    @LuaFunction(mainThread = true)
    fun isWorkingEnabled(): MethodResult = MethodResult.of(controllable.isWorkingEnabled)

    @LuaFunction(mainThread = true)
    fun setWorkingEnabled(enabled: Boolean) {
        controllable.isWorkingEnabled = enabled
    }

    @LuaFunction(mainThread = true)
    fun setSuspendAfterFinish(suspendAfterFinish: Boolean) {
        controllable.setSuspendAfterFinish(suspendAfterFinish)
    }
}
