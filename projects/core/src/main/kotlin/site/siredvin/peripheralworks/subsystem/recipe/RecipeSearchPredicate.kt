package site.siredvin.peripheralworks.subsystem.recipe

import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe


fun interface RecipeSearchPredicate {
    fun test(stack: ItemStack, recipe: Recipe<Container>, checkMode: NBTCheckMode): Boolean
}