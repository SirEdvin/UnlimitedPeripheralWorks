package site.siredvin.peripheralworks.subsystem.recipe.integration

import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.crafting.ShapedRecipe
import site.siredvin.peripheralworks.subsystem.recipe.RecipeTransformer

object ShapedCraftingRecipeTransformer : RecipeTransformer<CraftingContainer, ShapedRecipe>() {
    override fun getExtraData(recipe: ShapedRecipe): MutableMap<String, Any>? = mutableMapOf(
        "width" to recipe.width,
        "height" to recipe.height,
    )
}
