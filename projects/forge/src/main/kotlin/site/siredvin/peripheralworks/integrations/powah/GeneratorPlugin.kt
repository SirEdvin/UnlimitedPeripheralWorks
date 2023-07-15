package site.siredvin.peripheralworks.integrations.powah

import dan200.computercraft.api.lua.LuaFunction
import owmii.powah.lib.block.AbstractEnergyProvider

class GeneratorPlugin(private val provider: AbstractEnergyProvider<*>) : BaseEnergyStoragePlugin(provider) {

    @LuaFunction(mainThread = true)
    fun getEnergyGeneration(): Long {
        return provider.generation
    }
}
