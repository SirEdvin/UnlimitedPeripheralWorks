package site.siredvin.peripheralworks.integrations.automobility

import dan200.computercraft.api.lua.LuaFunction
import io.github.foundationgames.automobility.entity.AutomobileEntity
import net.minecraft.nbt.CompoundTag
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin

class AutomobilePlugin(private val entity: AutomobileEntity): IPeripheralPlugin {
    @LuaFunction(mainThread = true)
    fun control(signals: Map<*, *>) {
        val baseData = CompoundTag()
        entity.addAdditionalSaveData(baseData)
        signals.forEach {
            baseData.putBoolean(it.key.toString(), it.value as Boolean)
        }
        entity.readAdditionalSaveData(baseData)
        entity.markDirty()
    }
}