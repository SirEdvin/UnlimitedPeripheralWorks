package site.siredvin.peripheralworks.integrations.create

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe
import net.minecraft.core.RegistryAccess
import net.minecraft.world.Container
import site.siredvin.peripheralworks.subsystem.recipe.RecipeRegistryToolkit
import site.siredvin.peripheralworks.subsystem.recipe.RecipeTransformer

class CreateSequenceRecipeTransformer : RecipeTransformer<Container, SequencedAssemblyRecipe>() {

    override fun getInputs(recipe: SequencedAssemblyRecipe, registryAccess: RegistryAccess): List<*> = listOf(recipe.ingredient)

    override fun getExtraData(recipe: SequencedAssemblyRecipe): MutableMap<String, Any> = mutableMapOf(
        "loops" to recipe.loops,
        "sequence" to recipe.sequence.map { RecipeRegistryToolkit.serializeJson(it.toJson()) },
    )
}
