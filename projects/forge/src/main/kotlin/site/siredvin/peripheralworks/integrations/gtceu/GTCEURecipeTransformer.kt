package site.siredvin.peripheralworks.integrations.gtceu

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe
import net.minecraft.world.item.crafting.RecipeInput
import site.siredvin.peripheralworks.subsystem.recipe.RecipeTransformer

class GTCEURecipeTransformer : RecipeTransformer<RecipeInput, GTRecipe>() {
    override fun getExtraData(recipe: GTRecipe): MutableMap<String, Any> {
        val base = mutableMapOf<String, Any>()
        base["duration"] = recipe.duration
        base["parallels"] = recipe.parallels
        base["ocLevel"] = recipe.ocLevel
        if (recipe.tickInputs.contains(EURecipeCapability.CAP)) {
            val content = recipe.tickInputs[EURecipeCapability.CAP]!!.first()
            if (content.content is Number) {
                base["euCost"] = (content.content as Number).toLong()
            } else {
                base["euCost"] = (content.content as EnergyStack).totalEU
            }
        }
        return base
    }
}
