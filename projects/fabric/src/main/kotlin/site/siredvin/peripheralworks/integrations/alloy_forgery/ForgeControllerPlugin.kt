package site.siredvin.peripheralworks.integrations.alloy_forgery

import dan200.computercraft.api.lua.LuaFunction
import net.minecraft.nbt.CompoundTag
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import wraith.alloyforgery.block.ForgeControllerBlockEntity

class ForgeControllerPlugin(private val entity: ForgeControllerBlockEntity) : IPeripheralPlugin {
    override val additionalType: String
        get() = "alloy_forgery"

    @LuaFunction(mainThread = true)
    fun inspect(): Map<String, Any> {
        val entityData = CompoundTag()
        entity.saveAdditional(entityData)
        return mapOf(
            "fuel" to entityData.getInt("Fuel"),
            "currentSmeltTime" to entity.currentSmeltTime,
            "smeltProgress" to entity.smeltProgress,
        )
    }
}
