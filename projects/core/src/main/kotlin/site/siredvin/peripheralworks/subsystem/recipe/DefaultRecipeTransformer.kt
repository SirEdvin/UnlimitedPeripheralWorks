package site.siredvin.peripheralworks.subsystem.recipe

import net.minecraft.core.RegistryAccess
import net.minecraft.world.Container
import net.minecraft.world.item.crafting.Recipe


object DefaultRecipeTransformer: RecipeTransformer<Container, Recipe<Container>>() {
    override fun getInputs(recipe: Recipe<Container>): List<*> {
        return recipe.ingredients
    }

    override fun getOutputs(recipe: Recipe<Container>): List<*> {
        return listOf(recipe.getResultItem(RegistryAccess.EMPTY))
    }
}