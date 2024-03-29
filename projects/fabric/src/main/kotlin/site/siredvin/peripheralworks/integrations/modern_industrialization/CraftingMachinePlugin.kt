package site.siredvin.peripheralworks.integrations.modern_industrialization

import aztech.modern_industrialization.api.machine.component.CrafterAccess
import aztech.modern_industrialization.api.machine.holder.CrafterComponentHolder
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import site.siredvin.peripheralium.api.peripheral.IPeripheralPlugin
import site.siredvin.peripheralworks.api.PeripheralPluginProvider

class CraftingMachinePlugin(private val crafter: CrafterAccess) : IPeripheralPlugin {
    companion object {
        const val PLUGIN_TYPE = "mi_crafter"
    }

    object Provider : PeripheralPluginProvider {
        override val pluginType: String
            get() = PLUGIN_TYPE

        override fun provide(level: Level, pos: BlockPos, side: Direction): IPeripheralPlugin? {
            if (!Configuration.enableCraftingMachine) {
                return null
            }
            val blockEntity = level.getBlockEntity(pos) ?: return null
            if (blockEntity is CrafterComponentHolder) {
                return CraftingMachinePlugin(blockEntity.crafterComponent)
            }
            return null
        }
    }

    override val additionalType: String
        get() = PLUGIN_TYPE

    @LuaFunction(mainThread = true)
    fun isBusy(): Boolean {
        return crafter.hasActiveRecipe()
    }

    @LuaFunction(mainThread = true)
    fun getCraftingInformation(): MethodResult {
        if (!crafter.hasActiveRecipe()) {
            return MethodResult.of(emptyMap<String, Any>())
        }
        return MethodResult.of(
            mapOf(
                "progress" to crafter.progress,
                "currentEfficiency" to crafter.efficiencyTicks,
                "maxEfficiency" to crafter.maxEfficiencyTicks,
                "baseRecipeCost" to crafter.baseRecipeEu,
                "currentRecipeCost" to crafter.currentRecipeEu,
            ),
        )
    }
}
