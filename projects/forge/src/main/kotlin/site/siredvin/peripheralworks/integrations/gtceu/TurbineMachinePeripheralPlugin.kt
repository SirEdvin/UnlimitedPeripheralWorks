package site.siredvin.peripheralworks.integrations.gtceu

import com.gregtechceu.gtceu.api.capability.ITurbineMachine
import com.gregtechceu.gtceu.integration.cctweaked.peripherals.TurbineMachinePeripheral
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

class TurbineMachinePeripheralPlugin(private val capability: ITurbineMachine) : IPeripheralPlugin {
    companion object {
        const val TYPE = "gtceu:turbine_machine"
    }

    override val additionalType: String
        get() = TYPE

    @LuaFunction(mainThread = true)
    fun hasRotor(): MethodResult = TurbineMachinePeripheral.hasRotor(capability)

    @LuaFunction(mainThread = true)
    fun getRotorSpeed(): MethodResult = TurbineMachinePeripheral.getRotorSpeed(capability)

    @LuaFunction(mainThread = true)
    fun getMaxRotorHolderSpeed(): MethodResult = TurbineMachinePeripheral.getMaxRotorHolderSpeed(capability)

    @LuaFunction(mainThread = true)
    fun getTotalEfficiency(): MethodResult = TurbineMachinePeripheral.getTotalEfficiency(capability)

    @LuaFunction(mainThread = true)
    fun getCurrentProduction(): MethodResult = TurbineMachinePeripheral.getCurrentProduction(capability)

    @LuaFunction(mainThread = true)
    fun getOverclockVoltage(): MethodResult = TurbineMachinePeripheral.getOverclockVoltage(capability)

    @LuaFunction(mainThread = true)
    fun getRotorDurabilityPercent(): MethodResult = TurbineMachinePeripheral.getRotorDurabilityPercent(capability)
}
