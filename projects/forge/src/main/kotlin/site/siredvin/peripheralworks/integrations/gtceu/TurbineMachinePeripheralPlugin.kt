package site.siredvin.peripheralworks.integrations.gtceu

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IRotorHolderMachine
import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeTurbineMachine
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

class TurbineMachinePeripheralPlugin(private val turbine: LargeTurbineMachine) : IPeripheralPlugin {
    companion object {
        const val TYPE = "gtceu:turbine_machine"
    }

    override val additionalType: String
        get() = TYPE

    // GTCEu 7.0.2 has no turbine generic source; read the same native part used by its UI.
    private fun rotor(): IRotorHolderMachine? = turbine.parts.filterIsInstance<IRotorHolderMachine>().firstOrNull()

    @LuaFunction(mainThread = true)
    fun hasRotor(): MethodResult = MethodResult.of(rotor()?.hasRotor() ?: false)

    @LuaFunction(mainThread = true)
    fun getRotorSpeed(): MethodResult = MethodResult.of(rotor()?.rotorSpeed ?: 0)

    @LuaFunction(mainThread = true)
    fun getMaxRotorHolderSpeed(): MethodResult = MethodResult.of(rotor()?.maxRotorHolderSpeed ?: 0)

    @LuaFunction(mainThread = true)
    fun getTotalEfficiency(): MethodResult = MethodResult.of(rotor()?.totalEfficiency ?: 0)

    @LuaFunction(mainThread = true)
    fun getCurrentProduction(): MethodResult = MethodResult.of(if (turbine.isActive) turbine.recipeLogic.lastRecipe?.outputEUt?.voltage() ?: 0L else 0L)

    @LuaFunction(mainThread = true)
    fun getOverclockVoltage(): MethodResult = MethodResult.of(turbine.overclockVoltage)

    @LuaFunction(mainThread = true)
    fun getRotorDurabilityPercent(): MethodResult = MethodResult.of(rotor()?.rotorDurabilityPercent ?: 0)
}
