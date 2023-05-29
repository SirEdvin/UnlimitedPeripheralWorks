package site.siredvin.peripheralworks.integrations.occultism

import com.github.klikli_dev.occultism.common.blockentity.GoldenSacrificialBowlBlockEntity
import dan200.computercraft.api.lua.LuaFunction
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin

class GoldenSacrificialBowlPlugin(private val bowl: GoldenSacrificialBowlBlockEntity) : IPeripheralPlugin {

    @LuaFunction(mainThread = true)
    fun isBusy(): Boolean {
        return bowl.currentRitualRecipe != null
    }

    @LuaFunction(mainThread = true)
    fun getCraftingInformation(): Map<String, Any>? {
        if (bowl.currentRitualRecipe == null) {
            return null
        }
        return mapOf(
            "pentacle" to bowl.currentRitualRecipe.pentacleId.toString(),
            "ritual" to bowl.currentRitualRecipe.ritual.ritualID,
            "itemUseFulfilled" to bowl.itemUseFulfilled(),
            "sacrificeFulfilled" to bowl.sacrificeFulfilled(),
            "leftTime" to bowl.currentRitualRecipe.duration - bowl.currentTime,
        )
    }
}
