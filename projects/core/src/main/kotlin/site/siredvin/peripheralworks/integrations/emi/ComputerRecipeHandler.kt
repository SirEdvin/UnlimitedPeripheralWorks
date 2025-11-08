package site.siredvin.peripheralworks.integrations.emi

import dan200.computercraft.shared.computer.inventory.ComputerMenuWithoutInventory
import dev.emi.emi.api.recipe.EmiPlayerInventory
import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.handler.EmiCraftContext
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import site.siredvin.peripheralworks.networking.ClientNetworking
import site.siredvin.peripheralworks.networking.MapBasedEventMessage

class ComputerRecipeHandler : EmiRecipeHandler<ComputerMenuWithoutInventory> {
    override fun getInventory(p0: AbstractContainerScreen<ComputerMenuWithoutInventory>): EmiPlayerInventory = EmiPlayerInventory(emptyList())

    override fun supportsRecipe(p0: EmiRecipe): Boolean = true

    override fun canCraft(
        p0: EmiRecipe,
        p1: EmiCraftContext<ComputerMenuWithoutInventory>,
    ): Boolean = true

    override fun craft(
        p0: EmiRecipe,
        p1: EmiCraftContext<ComputerMenuWithoutInventory>,
    ): Boolean {
        val recipe = mutableMapOf<String, Any>()
        recipe["category"] = p0.category.id.toString()
        recipe["id"] = p0.id?.toString() ?: "unknown"
        recipe["inputs"] = p0.inputs.map(CommonEntrypoint::mapIngredient)
        recipe["outputs"] = p0.outputs.map(CommonEntrypoint::mapIngredient)
        ClientNetworking.sendToServer(MapBasedEventMessage(p1.screenHandler, "emi_recipe_paste", recipe))
        return true
    }
}
