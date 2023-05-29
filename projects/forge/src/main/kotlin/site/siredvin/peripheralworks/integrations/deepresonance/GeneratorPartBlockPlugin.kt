package site.siredvin.peripheralworks.integrations.deepresonance

import dan200.computercraft.api.lua.LuaFunction
import mcjty.deepresonance.modules.generator.block.GeneratorPartTileEntity
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin

class GeneratorPartBlockPlugin(private val controller: GeneratorPartTileEntity) : IPeripheralPlugin {
    @LuaFunction(mainThread = true)
    fun inspect(): Map<String, Any> {
        val blob = controller.blob
        return mapOf(
            "energy" to blob.energy,
            "lastRfPerTick" to blob.lastRfPerTick,
            "isActive" to blob.isActive,
        )
    }
}
