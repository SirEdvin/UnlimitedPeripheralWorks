package site.siredvin.peripheralworks.integrations.create

import com.simibubi.create.content.processing.recipe.ProcessingRecipe
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams
import net.minecraft.core.RegistryAccess
import net.minecraft.world.item.crafting.RecipeInput
import site.siredvin.peripheralworks.subsystem.recipe.RecipeTransformer

class CreateProcessingRecipeTransformer : RecipeTransformer<RecipeInput, ProcessingRecipe<RecipeInput, ProcessingRecipeParams>>() {
    override fun getInputs(recipe: ProcessingRecipe<RecipeInput, ProcessingRecipeParams>, registryAccess: RegistryAccess): List<*> = recipe.ingredients + recipe.fluidIngredients

    override fun getOutputs(recipe: ProcessingRecipe<RecipeInput, ProcessingRecipeParams>, registryAccess: RegistryAccess): List<*> = recipe.rollableResults + recipe.fluidResults

    override fun getExtraData(recipe: ProcessingRecipe<RecipeInput, ProcessingRecipeParams>): MutableMap<String, Any> = mutableMapOf(
        "heatRequirements" to recipe.requiredHeat.name,
        "processingDuration" to recipe.processingDuration,
    )
}
