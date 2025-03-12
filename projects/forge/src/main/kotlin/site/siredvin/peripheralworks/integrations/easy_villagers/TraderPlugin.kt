package site.siredvin.peripheralworks.integrations.easy_villagers

import dan200.computercraft.api.lua.LuaFunction
import de.maxhenkel.easyvillagers.blocks.tileentity.TraderTileentityBase
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin
import site.siredvin.tweakium.modules.peripheral.representation.LuaRepresentation
import site.siredvin.tweakium.modules.peripheral.representation.merchantData
import site.siredvin.tweakium.modules.peripheral.representation.villagerData

open class TraderPlugin(private val trader: TraderTileentityBase) : IPeripheralPlugin {
    override val additionalType: String
        get() = "easy_trader"

    @LuaFunction(mainThread = true)
    fun hasVillager(): Boolean = trader.hasVillager()

    @LuaFunction(mainThread = true)
    fun inspect(): Map<String, Any>? {
        if (!trader.hasVillager()) {
            return null
        }
        val villager = trader.villagerEntity
        val base = LuaRepresentation.forEntity(villager)
        merchantData.accept(villager, base)
        villagerData.accept(villager, base)
        return base
    }
}
