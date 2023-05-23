package site.siredvin.peripheralworks.integrations.deepresonance

import dan200.computercraft.api.lua.LuaFunction
import mcjty.deepresonance.modules.core.block.ResonatingCrystalTileEntity
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin

class CrystalPlugin(private val crystal: ResonatingCrystalTileEntity): IPeripheralPlugin {
    @LuaFunction(mainThread = true)
    fun inspect(): Map<String, Any> {
        return mapOf(
            "strength" to crystal.strength,
            "efficiency" to crystal.efficiency,
            "power" to crystal.power,
            "purity" to crystal.purity,
            "glowing" to crystal.isGlowing,
        )
    }
}