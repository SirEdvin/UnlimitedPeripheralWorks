package site.siredvin.peripheralworks.integrations.gtceu

import com.gregtechceu.gtceu.api.machine.MachineDefinition
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import site.siredvin.broccolium.modules.platform.PlatformRegistries
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

open class MachinePlugin(private val def: MachineDefinition) : IPeripheralPlugin {
    companion object {
        val TYPE = "gtceu:machine"
    }
    override val additionalType: String
        get() = TYPE

    @LuaFunction
    fun getRecipeTypes(): MethodResult = MethodResult.of(def.recipeTypes.map { PlatformRegistries.RECIPE_TYPES.getKey(it).toString() })
}
