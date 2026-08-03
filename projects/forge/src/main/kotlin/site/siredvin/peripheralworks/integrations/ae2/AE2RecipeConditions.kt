package site.siredvin.peripheralworks.integrations.ae2

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.data.recipes.FinishedRecipe
import java.util.function.Consumer

object AE2RecipeConditions {
    fun wrap(output: Consumer<FinishedRecipe>) = Consumer<FinishedRecipe> { recipe ->
        output.accept(object : FinishedRecipe by recipe {
            override fun serializeRecipeData(json: JsonObject) {
                recipe.serializeRecipeData(json)
                json.add(
                    "forge:conditions",
                    JsonArray().apply {
                        add(
                            JsonObject().apply {
                                addProperty("type", "forge:mod_loaded")
                                addProperty("modid", "ae2")
                            },
                        )
                    },
                )
            }
        })
    }
}
