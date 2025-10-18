package site.siredvin.peripheralworks.integrations.create

import com.simibubi.create.content.processing.recipe.ProcessingRecipe
import net.minecraft.core.RegistryAccess
import net.minecraft.world.Container
import site.siredvin.peripheralworks.subsystem.recipe.RecipeTransformer

class CreateProcessingRecipeTransformer : RecipeTransformer<Container, ProcessingRecipe<Container>>() {
    override fun getInputs(recipe: ProcessingRecipe<Container>, registryAccess: RegistryAccess): List<*> = recipe.ingredients + recipe.fluidIngredients

    override fun getOutputs(recipe: ProcessingRecipe<Container>, registryAccess: RegistryAccess): List<*> = recipe.rollableResults + recipe.fluidResults

    override fun getExtraData(recipe: ProcessingRecipe<Container>): MutableMap<String, Any> = mutableMapOf(
        "heatRequirements" to recipe.requiredHeat.name,
        "processingDuration" to recipe.processingDuration,
    )
}
