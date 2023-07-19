package site.siredvin.peripheralworks.integrations.powah

import dan200.computercraft.api.lua.LuaException
import dan200.computercraft.api.lua.LuaFunction
import owmii.powah.lib.logistics.IRedstoneInteract
import owmii.powah.lib.logistics.Redstone
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin

class RedstoneControlPlugin(private val something: IRedstoneInteract) : IPeripheralPlugin {
    @LuaFunction(mainThread = true)
    fun getRedstoneMode(): String {
        return something.redstoneMode.name.lowercase()
    }

    @LuaFunction(mainThread = true)
    fun setRedstoneMode(value: String) {
        try {
            something.redstoneMode = Redstone.valueOf(value.uppercase())
        } catch (_: IllegalArgumentException) {
            throw LuaException("Redstone mode must be one of: ${Redstone.values().joinToString { it.name.lowercase() }}")
        }
    }
}
