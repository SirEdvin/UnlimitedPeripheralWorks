package site.siredvin.peripheralworks.subsystem.recipe

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeInput

fun interface RecipeSearchPredicate {
    fun test(stack: ItemStack, recipe: Recipe<RecipeInput>, checkMode: NBTCheckMode): Boolean
}
