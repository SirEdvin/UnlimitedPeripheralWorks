package site.siredvin.peripheralworks.subsystem.recipe

import net.minecraft.core.RegistryAccess
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeInput

object DefaultRecipeTransformer : RecipeTransformer<RecipeInput, Recipe<RecipeInput>>() {
    override fun getInputs(recipe: Recipe<RecipeInput>, registryAccess: RegistryAccess): List<*> = recipe.ingredients

    override fun getOutputs(recipe: Recipe<RecipeInput>, registryAccess: RegistryAccess): List<*> = listOf(recipe.getResultItem(registryAccess))
}
