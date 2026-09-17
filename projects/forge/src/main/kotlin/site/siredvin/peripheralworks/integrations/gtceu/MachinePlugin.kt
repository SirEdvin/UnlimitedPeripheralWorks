package site.siredvin.peripheralworks.integrations.gtceu

import com.gregtechceu.gtceu.api.machine.MetaMachine
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.core.Direction
import site.siredvin.broccolium.modules.platform.PlatformRegistries
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

open class MachinePlugin(private val machine: MetaMachine, private val side: Direction) : IPeripheralPlugin {
    companion object {
        val TYPE = "gtceu:machine"
    }
    override val additionalType: String
        get() = TYPE

    @LuaFunction
    fun getRecipeTypes(): MethodResult = MethodResult.of(machine.definition.recipeTypes.map { PlatformRegistries.RECIPE_TYPES.getKey(it).toString() })

    @LuaFunction(mainThread = true)
    fun getInfo(): Map<String, Any> = MachineInfo.collect(machine, side)
}
