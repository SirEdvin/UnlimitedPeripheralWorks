package site.siredvin.peripheralworks.integrations.emi

import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.lua.MethodResult
import dev.emi.emi.registry.EmiRecipes
import net.minecraft.resources.ResourceLocation
import site.siredvin.broccolium.modules.platform.PlatformToolkit
import site.siredvin.tweakium.modules.peripheral.api.IPeripheralPlugin

class EmiRecipePeripheralPlugin : IPeripheralPlugin {
    @LuaFunction
    fun getEMI(recipeID: String): MethodResult {
        val id = ResourceLocation.tryParse(recipeID) ?: return MethodResult.of(null, "Incorrect recipe id")
        val recipe = EmiRecipes.manager.getRecipe(id) ?: return MethodResult.of(null, "Incorrect recipe id")
        return MethodResult.of(CommonEntrypoint.mapRecipe(recipe, PlatformToolkit.get().minecraftServer!!.registryAccess()))
    }
}
