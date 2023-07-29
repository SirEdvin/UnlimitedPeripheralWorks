package site.siredvin.peripheralworks.integrations.automobility

import dan200.computercraft.api.lua.LuaFunction
import io.github.foundationgames.automobility.entity.AutomobileEntity
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin

class AutomobilePlugin(private val entity: AutomobileEntity) : IPeripheralPlugin {

    @LuaFunction(mainThread = true)
    fun rotate(delta: Double) {
        entity.yRot += delta.toFloat()
    }

    @LuaFunction(mainThread = true)
    fun boost(power: Double, time: Int) {
        entity.boost(power.toFloat(), time)
    }
}
