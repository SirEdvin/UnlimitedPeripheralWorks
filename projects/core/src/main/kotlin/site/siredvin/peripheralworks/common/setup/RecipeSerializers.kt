package site.siredvin.peripheralworks.common.setup

import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer
import site.siredvin.peripheralworks.common.recipes.AnchorCleanRecipe
import site.siredvin.peripheralworks.common.recipes.AnchorCloningRecipe
import site.siredvin.peripheralworks.common.recipes.StatueCleanRecipe
import site.siredvin.peripheralworks.common.recipes.StatueCloningRecipe
import site.siredvin.peripheralworks.utils.modId
import site.siredvin.peripheralworks.xplat.PeripheralWorksPlatform

object RecipeSerializers {
    val STATUE_CLONING = PeripheralWorksPlatform.registerRecipeSerializer(
        modId("statue_cloning"),
        SimpleCraftingRecipeSerializer(::StatueCloningRecipe),
    )
    val STATUE_CLEAN = PeripheralWorksPlatform.registerRecipeSerializer(
        modId("statue_clean"),
        SimpleCraftingRecipeSerializer(::StatueCleanRecipe),
    )
    val ANCHOR_CLEAN = PeripheralWorksPlatform.registerRecipeSerializer(
        modId("anchor_clean"),
        SimpleCraftingRecipeSerializer(::AnchorCleanRecipe),
    )
    val ANCHOR_CLONING = PeripheralWorksPlatform.registerRecipeSerializer(
        modId("anchor_cloning"),
        SimpleCraftingRecipeSerializer(::AnchorCloningRecipe),
    )

    fun doSomething() {}
}
