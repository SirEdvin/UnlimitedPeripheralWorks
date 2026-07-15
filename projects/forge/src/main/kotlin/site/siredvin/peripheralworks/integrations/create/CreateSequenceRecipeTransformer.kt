package site.siredvin.peripheralworks.integrations.create

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe
import net.minecraft.core.RegistryAccess
import net.minecraftforge.items.wrapper.RecipeWrapper
import site.siredvin.peripheralworks.subsystem.recipe.RecipeRegistryToolkit
import site.siredvin.peripheralworks.subsystem.recipe.RecipeTransformer

class CreateSequenceRecipeTransformer : RecipeTransformer<RecipeWrapper, SequencedAssemblyRecipe>() {

    override fun getInputs(recipe: SequencedAssemblyRecipe, registryAccess: RegistryAccess): List<*> = listOf(recipe.ingredient)

    override fun getExtraData(recipe: SequencedAssemblyRecipe): MutableMap<String, Any> = mutableMapOf(
        "loops" to recipe.loops,
        "sequence" to recipe.sequence.map { RecipeRegistryToolkit.serializeJson(it.toJson()) },
    )
}
