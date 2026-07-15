package site.siredvin.peripheralworks.integrations.alloy_forgery

import dan200.computercraft.api.lua.LuaFunction
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import wraith.alloyforgery.block.ForgeControllerBlockEntity

class ForgeControllerPlugin(private val entity: ForgeControllerBlockEntity) : IPeripheralPlugin {
    override val additionalType: String
        get() = "alloy_forgery"

    @LuaFunction(mainThread = true)
    fun inspect(): Map<String, Any> {
        val entityData = entity.saveWithoutMetadata(entity.level!!.registryAccess())
        return mapOf(
            "fuel" to entityData.getInt("Fuel"),
            "currentSmeltTime" to entity.currentSmeltTime,
            "smeltProgress" to entity.smeltProgress,
        )
    }
}
