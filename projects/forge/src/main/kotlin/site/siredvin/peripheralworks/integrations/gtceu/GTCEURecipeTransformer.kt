package site.siredvin.peripheralworks.integrations.gtceu

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import net.minecraft.world.Container
import site.siredvin.peripheralworks.subsystem.recipe.RecipeTransformer

class GTCEURecipeTransformer : RecipeTransformer<Container, GTRecipe>() {
    override fun getExtraData(recipe: GTRecipe): MutableMap<String, Any> {
        val base = mutableMapOf<String, Any>()
        base["duration"] = recipe.duration
        base["parallels"] = recipe.parallels
        base["ocLevel"] = recipe.ocLevel
        if (recipe.tickInputs.contains(EURecipeCapability.CAP)) {
            val content = recipe.tickInputs[EURecipeCapability.CAP]!!.first()
            base["euCost"] = content.content as Long
        }
        return base
    }
}
