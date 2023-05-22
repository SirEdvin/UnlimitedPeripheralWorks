package site.siredvin.peripheralworks.integrations.easy_villagers

import dan200.computercraft.api.lua.LuaFunction
import de.maxhenkel.easyvillagers.blocks.tileentity.TraderTileentityBase
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralium.util.representation.LuaRepresentation
import site.siredvin.peripheralium.util.representation.merchantData
import site.siredvin.peripheralium.util.representation.villagerData

open class TraderPlugin(private val trader: TraderTileentityBase): IPeripheralPlugin {
    override val additionalType: String
        get() = "easy_trader"

    @LuaFunction(mainThread = true)
    fun hasVillager(): Boolean {
        return trader.hasVillager()
    }

    @LuaFunction(mainThread = true)
    fun inspect(): Map<String, Any>? {
        if (!trader.hasVillager())
            return null
        val villager = trader.villagerEntity
        val base = LuaRepresentation.forEntity(villager)
        merchantData.accept(villager, base)
        villagerData.accept(villager, base)
        return base
    }
}