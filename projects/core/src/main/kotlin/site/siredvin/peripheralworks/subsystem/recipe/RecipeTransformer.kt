package site.siredvin.peripheralworks.subsystem.recipe

import net.minecraft.core.RegistryAccess
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeInput
import site.siredvin.broccolium.modules.platform.api.RegistryEntry
import java.util.function.Consumer
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

abstract class RecipeTransformer<V : RecipeInput, T : Recipe<V>> {
    open fun getInputs(recipe: T, registryAccess: RegistryAccess): List<*> = recipe.ingredients

    open fun getOutputs(recipe: T, registryAccess: RegistryAccess): List<*> = listOf(recipe.getResultItem(registryAccess))

    open fun getExtraData(@Suppress("UNUSED_PARAMETER") recipe: T): MutableMap<String, Any>? = null

    protected fun serializeIngredients(originalData: List<*>): List<Any> = originalData.stream().map {
        // null in the ingredient lists may be important!
        //
        // For example, shaped minecraft crafting recipes contain null's
        // to indicate an empty slot. In order to retain the recipe shape,
        // the null's need to be preserved!
        it ?: RecipeRegistryToolkit.SERIALIZATION_EMPTY_SLOT
    }.map(RecipeRegistryToolkit::serialize).filter {
        it !== RecipeRegistryToolkit.SERIALIZATION_SKIP
    }.collect(Collectors.toList<Any>())

    fun transform(recipeEntry: RegistryEntry<T>, registryAccess: RegistryAccess): Map<String, Any> {
        val recipeData: MutableMap<String, Any> = HashMap()
        val recipe = recipeEntry.get()
        recipeData["id"] = recipeEntry.id.toString()
        recipeData["type"] = recipe.type.toString()
        recipeData["output"] = serializeIngredients(getOutputs(recipe, registryAccess))
        recipeData["input"] = serializeIngredients(getInputs(recipe, registryAccess))
        val extraData = getExtraData(recipe)
        if (extraData != null) {
            // extra cleanup
            val cleanupList: MutableList<String> = ArrayList()
            for (key in extraData.keys) {
                if (extraData[key] === RecipeRegistryToolkit.SERIALIZATION_SKIP) cleanupList.add(key)
            }
            cleanupList.forEach(Consumer { o: String -> extraData.remove(o) })
            recipeData["extra"] = extraData
        }
        return recipeData
    }
}
