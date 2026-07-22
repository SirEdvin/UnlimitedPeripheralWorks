package site.siredvin.peripheralworks.computercraft.plugins

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.shared.computer.blocks.AbstractComputerBlockEntity
import dan200.computercraft.shared.computer.blocks.ComputerPeripheral
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

class ComputerPlugin(type: String, val owner: AbstractComputerBlockEntity) : IPeripheralPlugin {

    private val internalPeripheral = ComputerPeripheral(type, owner)

    @LuaFunction
    fun turnOn() {
        internalPeripheral.turnOn()
    }

    @LuaFunction
    fun shutdown() {
        internalPeripheral.shutdown()
    }

    @LuaFunction
    fun reboot() {
        internalPeripheral.reboot()
    }

    @LuaFunction
    fun getID(): Int = internalPeripheral.id

    @LuaFunction
    fun isOn(): Boolean = internalPeripheral.isOn

    @LuaFunction
    fun getLabel(): String? = internalPeripheral.label
}
