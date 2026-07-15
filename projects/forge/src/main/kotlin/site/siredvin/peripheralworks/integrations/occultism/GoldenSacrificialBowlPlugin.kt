package site.siredvin.peripheralworks.integrations.occultism

import com.klikli_dev.occultism.common.blockentity.GoldenSacrificialBowlBlockEntity
import dan200.computercraft.api.lua.LuaFunction
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

class GoldenSacrificialBowlPlugin(private val bowl: GoldenSacrificialBowlBlockEntity) : IPeripheralPlugin {

    @LuaFunction(mainThread = true)
    fun isBusy(): Boolean = bowl.currentRitualRecipe != null

    @LuaFunction(mainThread = true)
    fun getCraftingInformation(): Map<String, Any>? {
        val recipe = bowl.currentRitualRecipe?.value() ?: return null
        return mapOf(
            "pentacle" to recipe.pentacleId.toString(),
            "ritual" to recipe.ritualType.toString(),
            "itemUseFulfilled" to bowl.itemUseFulfilled(),
            "sacrificeFulfilled" to bowl.sacrificeFulfilled(),
            "leftTime" to recipe.duration - bowl.currentTime,
        )
    }
}
