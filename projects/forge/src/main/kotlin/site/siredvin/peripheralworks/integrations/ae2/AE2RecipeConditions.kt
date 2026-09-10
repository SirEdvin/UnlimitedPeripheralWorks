package site.siredvin.peripheralworks.integrations.ae2

import net.minecraft.data.recipes.RecipeOutput
import net.neoforged.neoforge.common.conditions.ModLoadedCondition

object AE2RecipeConditions {
    fun wrap(output: RecipeOutput): RecipeOutput = output.withConditions(ModLoadedCondition("ae2"))
}
