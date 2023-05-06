package site.siredvin.peripheralworks.integrations.easy_villagers

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import de.maxhenkel.easyvillagers.blocks.tileentity.AutoTraderTileentity
import site.siredvin.peripheralium.util.assertBetween

class AutoTraderPlugin(private val autoTrader: AutoTraderTileentity): TraderPlugin(autoTrader) {
    override val additionalType: String
        get() = "easy_auto_trader"

    @LuaFunction(mainThread = true)
    fun getSelectedOffer(): Int {
        return autoTrader.tradeIndex + 1
    }

    @LuaFunction(mainThread = true)
    fun setSelectedOffer(index: Int): MethodResult {
        val villager = autoTrader.villagerEntity ?: return MethodResult.of(null, "Auto trader is empty")
        val maxTradeCount = villager.offers.size
        assertBetween(index, 1, maxTradeCount, "Trade count should be between 1 and $maxTradeCount")
        val realIndex = index - 1
        autoTrader.tradeIndex = realIndex
        return MethodResult.of(true)
    }
}